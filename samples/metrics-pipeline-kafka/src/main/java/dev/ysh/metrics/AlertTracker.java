package dev.ysh.metrics;

/**
 * 이상 한 번에 바로 위험 알림을 띄우지 않기 위한 3단계 상태.
 * NORMAL → (기준 초과) → PENDING → (연속 N번 초과) → CRITICAL
 * 기준 아래로 돌아오면 어느 단계에서든 NORMAL로 해제한다. 순간적인 튐은 PENDING에서 끝난다.
 */
public class AlertTracker {

    public enum State { NORMAL, PENDING, CRITICAL }

    private final double threshold;
    private final int criticalAfter;
    private State state = State.NORMAL;
    private int consecutiveBreaches;

    public AlertTracker(double threshold, int criticalAfter) {
        if (criticalAfter < 1) {
            throw new IllegalArgumentException("criticalAfter must be >= 1");
        }
        this.threshold = threshold;
        this.criticalAfter = criticalAfter;
    }

    public synchronized State evaluate(double observed) {
        if (observed > threshold) {
            consecutiveBreaches++;
            state = consecutiveBreaches >= criticalAfter ? State.CRITICAL : State.PENDING;
        } else {
            consecutiveBreaches = 0;
            state = State.NORMAL;
        }
        return state;
    }

    public synchronized State state() {
        return state;
    }
}
