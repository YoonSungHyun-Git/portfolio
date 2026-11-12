package dev.ysh.metrics;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemorySampleStore implements SampleStore {

    private final AtomicLong count = new AtomicLong();

    @Override
    public void save(MetricSample sample) {
        count.incrementAndGet();
    }

    @Override
    public long count() {
        return count.get();
    }
}
