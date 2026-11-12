package dev.ysh.metrics;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** host·metric 키마다 백분위 창과 알림 상태를 둔다. */
@Component
public class MetricAggregator {

    private final MetricsProperties props;
    private final Map<String, PercentileWindow> windows = new ConcurrentHashMap<>();
    private final Map<String, AlertTracker> alerts = new ConcurrentHashMap<>();

    public MetricAggregator(MetricsProperties props) {
        this.props = props;
    }

    /** 표본을 창에 넣고, 갱신된 P99로 알림 상태를 판정한다. */
    public AlertTracker.State accept(MetricSample sample) {
        PercentileWindow window = windows.computeIfAbsent(sample.key(),
                k -> new PercentileWindow(props.windowSize()));
        window.add(sample.value());
        AlertTracker tracker = alerts.computeIfAbsent(sample.key(),
                k -> new AlertTracker(props.alert().p99ThresholdMs(), props.alert().criticalAfter()));
        return tracker.evaluate(window.snapshot().p99());
    }

    public Optional<PercentileWindow.Snapshot> snapshot(String key) {
        return Optional.ofNullable(windows.get(key)).map(PercentileWindow::snapshot);
    }

    public AlertTracker.State alertState(String key) {
        AlertTracker tracker = alerts.get(key);
        return tracker == null ? AlertTracker.State.NORMAL : tracker.state();
    }
}
