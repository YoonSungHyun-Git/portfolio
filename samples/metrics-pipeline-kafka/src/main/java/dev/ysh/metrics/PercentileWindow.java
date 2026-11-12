package dev.ysh.metrics;

import java.util.Arrays;

/**
 * 최근 N개 표본으로 백분위를 계산하는 고정 크기 창.
 * 크기에 상한을 둬서 키가 많아져도 메모리가 끝없이 늘지 않는다.
 * 백분위는 nearest-rank 방식: 정렬한 뒤 ceil(p/100 * n)번째 값.
 */
public class PercentileWindow {

    private final double[] buffer;
    private int next;
    private int size;

    public PercentileWindow(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.buffer = new double[capacity];
    }

    public synchronized void add(double value) {
        buffer[next] = value;
        next = (next + 1) % buffer.length;
        if (size < buffer.length) {
            size++;
        }
    }

    public synchronized int size() {
        return size;
    }

    public synchronized Snapshot snapshot() {
        if (size == 0) {
            return new Snapshot(0, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        }
        double[] sorted = Arrays.copyOf(buffer, size);
        Arrays.sort(sorted);
        return new Snapshot(size, rank(sorted, 50), rank(sorted, 95), rank(sorted, 99), sorted[size - 1]);
    }

    private static double rank(double[] sorted, double p) {
        int index = (int) Math.ceil(p / 100.0 * sorted.length) - 1;
        return sorted[Math.max(0, index)];
    }

    /** P50이 그대로인데 P99만 오르면 일부 요청만 느린 것, P50까지 오르면 전체가 느려진 것. */
    public record Snapshot(int count, double p50, double p95, double p99, double max) {
    }
}
