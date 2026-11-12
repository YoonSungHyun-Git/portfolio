/*
 * "레거시" 신호 처리 코드 예제.
 * 실제 프로젝트 코드가 아니라, 포팅 검증 방식을 보여주기 위해 새로 작성한 C 코드입니다.
 * 이관 전 시스템의 동작을 그대로 담고 있다고 가정하고, Go 구현은 이 결과와 같아야 합니다.
 */
#include <math.h>
#include "signal.h"

/* 위상 펼치기: 인접 샘플 차이가 pi를 넘으면 2pi 단위로 보정해 누적한다. */
void unwrap_phase(const double *in, int n, double *out)
{
    if (n <= 0) {
        return;
    }
    double offset = 0.0;
    out[0] = in[0];
    for (int i = 1; i < n; i++) {
        double d = in[i] - in[i - 1];
        if (d > M_PI) {
            offset -= 2.0 * M_PI;
        } else if (d < -M_PI) {
            offset += 2.0 * M_PI;
        }
        out[i] = in[i] + offset;
    }
}

/* 이동 평균: 창 크기 w, 앞쪽 경계는 가능한 샘플만으로 평균한다. */
void moving_average(const double *in, int n, int w, double *out)
{
    if (n <= 0 || w <= 0) {
        return;
    }
    double sum = 0.0;
    for (int i = 0; i < n; i++) {
        sum += in[i];
        if (i >= w) {
            sum -= in[i - w];
        }
        int count = (i + 1 < w) ? (i + 1) : w;
        out[i] = sum / count;
    }
}
