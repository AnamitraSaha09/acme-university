package com.acme.university.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitingConfig {

    private long capacity = 50;

    private Duration refillInterval = Duration.ofSeconds(1);

    private long maxClients = 100_000;

    private Duration clientTtl = Duration.ofMinutes(10);

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public Duration getRefillInterval() {
        return refillInterval;
    }

    public void setRefillInterval(Duration refillInterval) {
        this.refillInterval = refillInterval;
    }

    public long getMaxClients() {
        return maxClients;
    }

    public void setMaxClients(long maxClients) {
        this.maxClients = maxClients;
    }

    public Duration getClientTtl() {
        return clientTtl;
    }

    public void setClientTtl(Duration clientTtl) {
        this.clientTtl = clientTtl;
    }
}
