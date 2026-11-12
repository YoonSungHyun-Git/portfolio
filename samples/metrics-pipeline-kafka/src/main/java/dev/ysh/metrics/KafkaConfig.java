package dev.ysh.metrics;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    NewTopic metricsTopic(MetricsProperties props) {
        return TopicBuilder.name(props.topic())
                .partitions(props.partitions())
                .replicas(props.replicas())
                // 3벌 중 최소 2벌에 써져야 성공. 1대가 죽어도 쓰기는 계속된다.
                .config("min.insync.replicas", props.replicas() >= 3 ? "2" : "1")
                .build();
    }

    /**
     * 처리 중 예외가 나면 오프셋을 커밋하지 않고 같은 레코드를 다시 처리한다(0.2초 간격 최대 5회).
     * 저장소가 잠깐 끊겨도 데이터를 잃지 않기 위해서다.
     */
    @Bean
    DefaultErrorHandler errorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(200L, 5L));
    }
}
