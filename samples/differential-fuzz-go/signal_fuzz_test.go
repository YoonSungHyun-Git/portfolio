package signal

import (
	"encoding/binary"
	"math"
	"testing"
)

const tolerance = 1e-7

// bytesToSamples는 퍼저가 만든 바이트를 유한한 float64 샘플로 바꿉니다.
// NaN·Inf와 지나치게 큰 값은 실제 계측 범위 밖이라 버립니다.
func bytesToSamples(b []byte) []float64 {
	samples := make([]float64, 0, len(b)/8)
	for len(b) >= 8 {
		v := math.Float64frombits(binary.LittleEndian.Uint64(b[:8]))
		b = b[8:]
		if math.IsNaN(v) || math.IsInf(v, 0) || math.Abs(v) > 1e6 {
			continue
		}
		samples = append(samples, v)
	}
	return samples
}

func assertSame(t *testing.T, name string, got, want []float64) {
	t.Helper()
	if len(got) != len(want) {
		t.Fatalf("%s: 길이 다름 got=%d want=%d", name, len(got), len(want))
	}
	for i := range got {
		if math.Abs(got[i]-want[i]) > tolerance {
			t.Fatalf("%s: index %d에서 불일치 go=%.12f c=%.12f", name, i, got[i], want[i])
		}
	}
}

func seed(f *testing.F) {
	f.Add([]byte{}, 3)
	buf := make([]byte, 8*64)
	for i := range 64 {
		binary.LittleEndian.PutUint64(buf[i*8:], math.Float64bits(math.Sin(float64(i))*4))
	}
	f.Add(buf, 5)
}

// FuzzUnwrapPhase: 무작위 입력에서 Go 구현이 레거시 C와 같은 결과를 내는지 비교합니다.
func FuzzUnwrapPhase(f *testing.F) {
	seed(f)
	f.Fuzz(func(t *testing.T, raw []byte, _ int) {
		in := bytesToSamples(raw)
		assertSame(t, "UnwrapPhase", UnwrapPhase(in), legacyUnwrapPhase(in))
	})
}

// FuzzMovingAverage: 창 크기까지 무작위로 바꿔 가며 비교합니다.
func FuzzMovingAverage(f *testing.F) {
	seed(f)
	f.Fuzz(func(t *testing.T, raw []byte, w int) {
		in := bytesToSamples(raw)
		w = 1 + int(uint(w)%32)
		assertSame(t, "MovingAverage", MovingAverage(in, w), legacyMovingAverage(in, w))
	})
}
