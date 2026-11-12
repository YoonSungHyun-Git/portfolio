package dev.ysh.metrics;

import org.junit.jupiter.api.Test;

import static dev.ysh.metrics.AlertTracker.State.CRITICAL;
import static dev.ysh.metrics.AlertTracker.State.NORMAL;
import static dev.ysh.metrics.AlertTracker.State.PENDING;
import static org.assertj.core.api.Assertions.assertThat;

class AlertTrackerTest {

    @Test
    void 연속으로_기준을_넘으면_Pending을_거쳐_Critical() {
        AlertTracker t = new AlertTracker(200, 3);
        assertThat(t.evaluate(250)).isEqualTo(PENDING);
        assertThat(t.evaluate(260)).isEqualTo(PENDING);
        assertThat(t.evaluate(270)).isEqualTo(CRITICAL);
    }

    @Test
    void 순간적인_튐은_Pending에서_해제된다() {
        AlertTracker t = new AlertTracker(200, 3);
        assertThat(t.evaluate(500)).isEqualTo(PENDING);
        assertThat(t.evaluate(50)).isEqualTo(NORMAL);
        assertThat(t.evaluate(500)).isEqualTo(PENDING);
    }

    @Test
    void Critical도_기준_아래로_돌아오면_해제된다() {
        AlertTracker t = new AlertTracker(200, 1);
        assertThat(t.evaluate(300)).isEqualTo(CRITICAL);
        assertThat(t.evaluate(100)).isEqualTo(NORMAL);
    }
}
