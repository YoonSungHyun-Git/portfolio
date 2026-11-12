package dev.ysh.metrics;

/** 시계열 저장소 자리. 실제 시스템에서는 InfluxDB 등이 들어간다. */
public interface SampleStore {

    void save(MetricSample sample);

    long count();
}
