// Package signal은 레거시 C 신호 처리 함수를 Go로 옮긴 구현입니다.
package signal

import "math"

// UnwrapPhase는 인접 샘플 차이가 pi를 넘을 때 2pi 단위로 보정해 위상을 펼칩니다.
func UnwrapPhase(in []float64) []float64 {
	out := make([]float64, len(in))
	if len(in) == 0 {
		return out
	}
	offset := 0.0
	out[0] = in[0]
	for i := 1; i < len(in); i++ {
		d := in[i] - in[i-1]
		switch {
		case d > math.Pi:
			offset -= 2 * math.Pi
		case d < -math.Pi:
			offset += 2 * math.Pi
		}
		out[i] = in[i] + offset
	}
	return out
}

// MovingAverage는 창 크기 w의 이동 평균을 구합니다. 앞쪽 경계는 가능한 샘플만으로 평균합니다.
func MovingAverage(in []float64, w int) []float64 {
	out := make([]float64, len(in))
	if len(in) == 0 || w <= 0 {
		return out
	}
	sum := 0.0
	for i, v := range in {
		sum += v
		if i >= w {
			sum -= in[i-w]
		}
		out[i] = sum / float64(min(i+1, w))
	}
	return out
}
