package dev.ysh.metrics;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/** 수집 에이전트 역할. 서버로 직접 보내지 않고 Kafka에 쓰기만 한다. */
@Component
public class MetricPublisher {

    private final KafkaTemplate<String, MetricSample> kafka;
    private final MetricsProperties props;

    public MetricPublisher(KafkaTemplate<String, MetricSample> kafka, MetricsProperties props) {
        this.kafka = kafka;
        this.props = props;
    }

    public CompletableFuture<SendResult<String, MetricSample>> publish(MetricSample sample) {
        return kafka.send(props.topic(), sample.key(), sample);
    }
}
