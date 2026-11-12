package signal

// 테스트 파일에서는 cgo를 쓸 수 없어서, 레거시 C 호출은 이 파일에 둡니다.

/*
#cgo CFLAGS: -I${SRCDIR}/legacy -D_USE_MATH_DEFINES
#cgo LDFLAGS: -lm
#include "legacy/signal.c"
*/
import "C"

import "unsafe"

func legacyUnwrapPhase(in []float64) []float64 {
	out := make([]float64, len(in))
	if len(in) == 0 {
		return out
	}
	C.unwrap_phase(
		(*C.double)(unsafe.Pointer(&in[0])), C.int(len(in)),
		(*C.double)(unsafe.Pointer(&out[0])),
	)
	return out
}

func legacyMovingAverage(in []float64, w int) []float64 {
	out := make([]float64, len(in))
	if len(in) == 0 {
		return out
	}
	C.moving_average(
		(*C.double)(unsafe.Pointer(&in[0])), C.int(len(in)), C.int(w),
		(*C.double)(unsafe.Pointer(&out[0])),
	)
	return out
}
