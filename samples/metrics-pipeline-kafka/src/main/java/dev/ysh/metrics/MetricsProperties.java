package dev.ysh.metrics;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "metrics")
public record MetricsProperties(String topic, int partitions, short replicas, int windowSize, Alert alert) {

    public record Alert(double p99ThresholdMs, int criticalAfter) {
    }
}
