package dev.ysh.eventingest;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "event-stream")
public record EventStreamProperties(int maxSubscribers, long timeoutMs) {
}
