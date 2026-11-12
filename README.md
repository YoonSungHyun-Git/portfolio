# 윤성현 · 백엔드 개발자

Java/Spring 백엔드 15년차. 결제·정산 서버를 9년간 운영했고, Kafka·RabbitMQ·MQTT로 실시간 데이터 파이프라인을 만들어 왔습니다.
최근에는 레거시 C 수집 서버를 Go로 재구축하고, 운영 장애의 원인을 실측으로 확정해 고치는 일을 하고 있습니다.

이 저장소는 회사 코드 없이 정리한 **아키텍처 문서**와, 핵심 기법을 새로 구현한 **샘플 코드**를 담고 있습니다.

## 아키텍처 문서

| 문서 | 핵심 |
|---|---|
| [01. 레거시 C 수집 서버의 Go 재구축](docs/01-collector-go-rebuild.md) | 왜 Go였나, 외부 API 41개 유지, 차분 퍼징으로 28,065회 입력 오차 1e-7 이하 확인 |
| [02. 운영 장애 분석](docs/02-incident-analysis.md) | gctrace로 98GB OOM 원인 확정, 고정 DMA 메모리 512MB → 256MB |
| [03. 알림 판정 품질 개선](docs/03-alert-quality.md) | 차량 오탐 줄이기, 궤적추적 기각 등 만들지 않기로 한 결정 |
| [04. MQTT 멀티노드 경보 장치](docs/04-edge-multinode-mqtt.md) | 노드 자가 등록, 인터넷 없는 현장의 캡티브 포털 앱 배포, 부팅 2~3분 → 31초 |
| [05. 시계열 수집·분석 파이프라인](docs/05-timeseries-pipelines.md) | Kafka로 하루 1TB(테스트 환경), Spring Boot 분석 API |
| [06. 결제·정산 백엔드](docs/06-payment-integration.md) | 중계기관을 통한 카드망 연계, HSM·AES256 |
| [07. 코딩 에이전트와 일하는 방식](docs/07-ai-workflow.md) | 에이전트 하네스(지침·메모리·훅·커맨드·탐색 도구), 직접 만든 위키 MCP, Claude·Gemini 교차 검증, AI 주장을 데이터로 반박한 사례 |

## 샘플 코드

| 샘플 | 내용 | 확인 |
|---|---|---|
| [event-ingest-spring](samples/event-ingest-spring) | Spring Boot 3 이벤트 수신 API + SSE 실시간 알림. 입구 검증, 커밋 후 알림, 구독자 상한 | `./gradlew test` 7개 통과 |
| [metrics-pipeline-kafka](samples/metrics-pipeline-kafka) | Kafka(KRaft)로 수집과 처리를 분리하고 P50·P95·P99와 단계별 알림(Normal·Pending·Critical)을 계산. 처리 후 커밋, 실패 시 재처리 | `./gradlew test` 8개 통과 (내장 KRaft 브로커) |
| [differential-fuzz-go](samples/differential-fuzz-go) | 레거시 C와 Go 구현을 같은 무작위 입력으로 비교하는 퍼징 | 각 15초 퍼징, 100만 회 이상 실행 불일치 0건 |

## 기술

- **Backend**: Java 17, Spring Boot 3, Spring Data JPA, MyBatis, Bean Validation, Quartz
- **Messaging**: Kafka, RabbitMQ, MQTT
- **Database**: PostgreSQL, Oracle(PL/SQL), InfluxDB
- **Infra**: Docker, Gradle, GitHub Actions
- **그 밖에**: Go, C, Python, TypeScript

## 연락처

ysh648@naver.com
