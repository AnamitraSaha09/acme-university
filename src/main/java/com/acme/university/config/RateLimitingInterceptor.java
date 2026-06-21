package com.acme.university.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Resiliency requirement: protect the database from excessive requests.
 *
 * <p>Implemented request throttling here using an open-source library of
 * your choice.
 * <ul>
 *   <li>Add the dependency in {@code pom.xml} (a placeholder is noted there).</li>
 *   <li>Decide the limiting key (global, per-client-IP, per-endpoint, ...).</li>
 *   <li>When the limit is exceeded, short-circuit the request: set the
 *       response status to {@code 429 Too Many Requests} and return
 *       {@code false}.</li>
 * </ul>
 * This interceptor is registered in {@link WebConfig}.
 */
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final Cache<String, Bucket> buckets;
    private final RateLimitingConfig config;

    public RateLimitingInterceptor(RateLimitingConfig config) {
        this.buckets = Caffeine.newBuilder()
                .maximumSize(config.getMaxClients())
                .expireAfterAccess(config.getClientTtl())
                .build();
        this.config = config;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // Consults the rate limiter; return false (and set HTTP 429) when
        //       the caller has exceeded their allowance. Currently a no-op.

        Bucket bucket = buckets.get(findClientKey(request), key -> newBucket());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", Long.toString(probe.getRemainingTokens()));
            return true;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        long waitForRefillSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
        response.addHeader(HttpHeaders.RETRY_AFTER, Long.toString(waitForRefillSeconds));
        writeBody(response, waitForRefillSeconds);

        return false;
    }

    private String findClientKey(HttpServletRequest request) {
        String forwardedHost = request.getHeader("X-Forwarded-For");
        if (forwardedHost == null || forwardedHost.isBlank()) {
            return request.getRemoteAddr();
        }
        return forwardedHost.split(",")[0];
    }

    private Bucket newBucket() {
        return Bucket.builder().addLimit(
                limit -> limit.capacity(config.getCapacity())
                        .refillGreedy(config.getCapacity(), config.getRefillInterval())
        ).build();
    }

    private void writeBody(HttpServletResponse response, long retryAfterSeconds) {
        String body = "{\"status\":429,\"error\":\"Too Many Requests\","
                + "\"message\":\"Rate limit exceeded. Retry after " + retryAfterSeconds
                + " second(s).\"}";
        try {
            response.getWriter().write(body);
        } catch (IOException ignored) {
            // Response already persisted.
        }
    }
}
