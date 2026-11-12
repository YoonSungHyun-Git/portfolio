# differential-fuzz-go

레거시 C 코드를 Go로 옮길 때, **두 구현에 같은 무작위 입력을 넣고 결과를 비교**해 동일성을 확인하는 방법을 보여주는 예제입니다.

실제 프로젝트 코드가 아니라 방식만 재구현했습니다. 실제 프로젝트에서는 광섬유 센서 신호 처리(위상 복조 등)를 이 방식으로 검증했고, 28,065회 입력에서 오차 1e-7 이하를 확인한 뒤 전환했습니다. → [설계 문서](../../docs/01-collector-go-rebuild.md)

## 구성

```
legacy/signal.c      이관 전 C 구현 (기준)
signal.go            Go로 옮긴 구현
legacy_bridge.go     cgo로 C 함수를 호출하는 다리 (테스트 파일에서는 cgo를 쓸 수 없어서 분리)
signal_fuzz_test.go  Go 퍼저가 만든 입력으로 두 구현을 비교
```

## 실행

C 컴파일러(gcc 등)가 필요합니다.

```bash
CGO_ENABLED=1 go test ./...
CGO_ENABLED=1 go test -run='^$' -fuzz=FuzzUnwrapPhase -fuzztime=30s .
CGO_ENABLED=1 go test -run='^$' -fuzz=FuzzMovingAverage -fuzztime=30s .
```

로컬 실행 결과(각 15초): `FuzzUnwrapPhase` 약 163만 회, `FuzzMovingAverage` 약 103만 회 실행, 불일치 0건.

## 실제 프로젝트에서 배운 점

**기준 코드는 실제 레거시 소스여야 합니다.** 처음에는 레거시를 손으로 옮겨 적은 C 재현 코드를 기준으로 썼는데, 2채널 모드의 DC 제거처럼 Go와 재현 코드가 *같이* 틀려서 테스트를 통과한 경우가 있었습니다. 그 뒤로는 기준 코드도 실제 레거시 소스와 대조합니다. 이 예제에서 `legacy/` 폴더를 따로 둔 이유입니다.

## 예제 설계

- 계측 데이터에 없는 NaN·Inf·비현실적으로 큰 값은 입력에서 걸러냅니다.
- 허용 오차는 실제 프로젝트와 같은 1e-7을 썼습니다.
