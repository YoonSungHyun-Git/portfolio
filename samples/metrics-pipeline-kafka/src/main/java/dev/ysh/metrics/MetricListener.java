package dev.ysh.metrics;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 서버 역할. Kafka에서 자기 속도로 읽어 간다.
 * 저장이 끝난 뒤에만 오프셋이 커밋되므로(ack-mode: record), 저장 중 실패하면 같은 레코드를 다시 받는다.
 */
@Component
public class MetricListener {

    private final SampleStore store;
    private final MetricAggregator aggregator;

    public MetricListener(SampleStore store, MetricAggregator aggregator) {
        this.store = store;
        this.aggregator = aggregator;
    }

    @KafkaListener(topics = "${metrics.topic}", concurrency = "${metrics.partitions}")
    public void onMessage(MetricSample sample) {
        store.save(sample);
        aggregator.accept(sample);
    }
}
