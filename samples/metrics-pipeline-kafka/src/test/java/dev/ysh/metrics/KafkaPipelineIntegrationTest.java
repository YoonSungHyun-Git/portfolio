package dev.ysh.metrics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * 내장 Kafka(KRaft 모드, ZooKeeper 없음)로 전체 흐름을 확인한다.
 * 발행 → 소비 → 저장 → 백분위 → 알림 상태, 그리고 저장 실패 시 재처리.
 */
@SpringBootTest(properties = {
        "metrics.replicas=1",
        "metrics.window-size=100",
        "metrics.alert.p99-threshold-ms=200",
        "metrics.alert.critical-after=3"
})
@EmbeddedKafka(kraft = true, partitions = 3, topics = "metrics",
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class KafkaPipelineIntegrationTest {

    /** 값 42.0을 처음 받으면 한 번 실패하는 저장소. 저장소가 잠깐 끊긴 상황을 흉내 낸다. */
    static class FlakyStore implements SampleStore {
        final AtomicLong saved = new AtomicLong();
        final AtomicInteger failures = new AtomicInteger();
        private final AtomicBoolean failedOnce = new AtomicBoolean();

        @Override
        public void save(MetricSample sample) {
            if (sample.value() == 42.0 && failedOnce.compareAndSet(false, true)) {
                failures.incrementAndGet();
                throw new IllegalStateException("저장소 일시 장애");
            }
            saved.incrementAndGet();
        }

        @Override
        public long count() {
            return saved.get();
        }
    }

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        FlakyStore flakyStore() {
            return new FlakyStore();
        }
    }

    @Autowired
    MetricPublisher publisher;

    @Autowired
    MetricAggregator aggregator;

    @Autowired
    FlakyStore store;

    private void publish(String host, double value) {
        publisher.publish(new MetricSample(host, "latency_ms", value, System.currentTimeMillis())).join();
    }

    @Test
    void 저장이_한_번_실패해도_유실_없이_백분위가_계산된다() {
        for (int i = 1; i <= 100; i++) {
            publish("db-1", i);
        }

        await().atMost(Duration.ofSeconds(30))
                .until(() -> aggregator.snapshot("db-1:latency_ms").map(s -> s.count() == 100).orElse(false));

        PercentileWindow.Snapshot s = aggregator.snapshot("db-1:latency_ms").orElseThrow();
        assertThat(s.p50()).isEqualTo(50);
        assertThat(s.p95()).isEqualTo(95);
        assertThat(s.p99()).isEqualTo(99);
        assertThat(store.failures.get()).isEqualTo(1);
        assertThat(aggregator.alertState("db-1:latency_ms")).isEqualTo(AlertTracker.State.NORMAL);
    }

    @Test
    void 긴_지연이_계속되면_Critical_돌아오면_해제() {
        for (int i = 0; i < 3; i++) {
            publish("db-2", 500);
        }
        await().atMost(Duration.ofSeconds(30))
                .until(() -> aggregator.alertState("db-2:latency_ms") == AlertTracker.State.CRITICAL);

        // 창 크기(100)만큼 정상 값이 들어오면 긴 지연이 밀려나 해제된다
        for (int i = 0; i < 100; i++) {
            publish("db-2", 10);
        }
        await().atMost(Duration.ofSeconds(30))
                .until(() -> aggregator.alertState("db-2:latency_ms") == AlertTracker.State.NORMAL);
    }
}
