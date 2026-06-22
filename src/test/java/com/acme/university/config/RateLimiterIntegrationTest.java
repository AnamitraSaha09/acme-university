package com.acme.university.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests the rate limiter, once the client exhausts its tokens,
 * further calls will get HTTP 429
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "rate-limit.capacity=2",
        "rate-limit.refill-interval=2s"
})
class RateLimiterIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void exceedingTheLimit_returnsTooManyRequests() throws Exception {
        // The first two requests pass (the resource is
        // absent thus returns 404, but consumes tokens).
        mockMvc.perform(get("/api/v1/lecturers/random")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/lecturers/random")).andExpect(status().isNotFound());

        // The third request exceeds the bucket and is throttled.
        mockMvc.perform(get("/api/v1/lecturers/random"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.detail").exists());

        // waits for 2 seconds = refill period
        Thread.sleep(2000);

        // Requests go through once the bucket has tokens again
        mockMvc.perform(get("/api/v1/lecturers/random")).andExpect(status().isNotFound());
    }
}
