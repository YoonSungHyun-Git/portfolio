package dev.ysh.metrics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PercentileWindowTest {

    @Test
    void 일부터_백까지면_P50_P95_P99가_50_95_99() {
        PercentileWindow w = new PercentileWindow(1000);
        for (int i = 100; i >= 1; i--) {
            w.add(i);
        }
        PercentileWindow.Snapshot s = w.snapshot();
        assertThat(s.count()).isEqualTo(100);
        assertThat(s.p50()).isEqualTo(50);
        assertThat(s.p95()).isEqualTo(95);
        assertThat(s.p99()).isEqualTo(99);
        assertThat(s.max()).isEqualTo(100);
    }

    @Test
    void 평균은_그대로여도_P99는_소수의_긴_지연을_드러낸다() {
        PercentileWindow w = new PercentileWindow(1000);
        for (int i = 0; i < 98; i++) {
            w.add(10);
        }
        w.add(900);
        w.add(900);
        PercentileWindow.Snapshot s = w.snapshot();
        assertThat(s.p50()).isEqualTo(10);
        assertThat(s.p99()).isEqualTo(900);
    }

    @Test
    void 창이_가득_차면_오래된_값부터_밀려난다() {
        PercentileWindow w = new PercentileWindow(3);
        w.add(1000);
        w.add(1);
        w.add(2);
        w.add(3);
        assertThat(w.size()).isEqualTo(3);
        assertThat(w.snapshot().max()).isEqualTo(3);
    }
}
