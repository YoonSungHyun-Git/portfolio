package dev.ysh.metrics;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricController {

    private final MetricPublisher publisher;
    private final MetricAggregator aggregator;

    public MetricController(MetricPublisher publisher, MetricAggregator aggregator) {
        this.publisher = publisher;
        this.aggregator = aggregator;
    }

    /** 수집 에이전트를 흉내 내 측정값을 Kafka에 넣는다. */
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void publish(@RequestBody MetricSample sample) {
        publisher.publish(sample);
    }

    public record Summary(String key, int count, double p50, double p95, double p99, double max,
                          AlertTracker.State alert) {
    }

    @GetMapping("/{host}/{metric}")
    public ResponseEntity<Summary> summary(@PathVariable String host, @PathVariable String metric) {
        String key = host + ":" + metric;
        return aggregator.snapshot(key)
                .map(s -> new Summary(key, s.count(), s.p50(), s.p95(), s.p99(), s.max(), aggregator.alertState(key)))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
