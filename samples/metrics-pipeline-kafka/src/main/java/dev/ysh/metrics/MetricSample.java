package dev.ysh.metrics;

/**
 * 수집 에이전트가 보내는 측정값 한 건. 예: host=db-1, metric=query_latency_ms, value=12.5
 */
public record MetricSample(String host, String metric, double value, long timestampMillis) {

    /** 같은 host·metric은 같은 파티션으로 가서 순서가 지켜진다. */
    public String key() {
        return host + ":" + metric;
    }
}
