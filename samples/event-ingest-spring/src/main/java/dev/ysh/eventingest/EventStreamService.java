package dev.ysh.eventingest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 새 이벤트를 SSE 구독자에게 바로 밀어준다.
 *
 * 설계 메모
 * - DB를 주기적으로 조회해 변화를 찾는 대신, 저장이 끝난 시점에 바로 broadcast한다(지연 0, 불필요한 쿼리 없음).
 * - 구독자 수에 상한을 둔다. 상한이 없으면 끊기지 않은 연결이 쌓여 메모리가 계속 는다.
 * - 전송에 실패한 연결은 그 자리에서 제거한다.
 */
@Service
public class EventStreamService {

    static final String EVENT_NAME = "event-created";
    private static final Logger log = LoggerFactory.getLogger(EventStreamService.class);

    private final EventStreamProperties props;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public EventStreamService(EventStreamProperties props) {
        this.props = props;
    }

    public SseEmitter subscribe() {
        if (emitters.size() >= props.maxSubscribers()) {
            throw new TooManySubscribersException(props.maxSubscribers());
        }
        SseEmitter emitter = new SseEmitter(props.timeoutMs());
        String id = UUID.randomUUID().toString();
        emitters.put(id, emitter);
        emitter.onCompletion(() -> emitters.remove(id));
        emitter.onTimeout(() -> {
            emitters.remove(id);
            emitter.complete();
        });
        emitter.onError(e -> emitters.remove(id));
        return emitter;
    }

    public void broadcast(EventDtos.Response event) {
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(EVENT_NAME)
                        .id(String.valueOf(event.id()))
                        .data(event));
            } catch (IOException | IllegalStateException e) {
                log.debug("SSE 전송 실패, 구독 해제 id={}", id);
                emitters.remove(id);
            }
        });
    }

    int subscriberCount() {
        return emitters.size();
    }

    public static class TooManySubscribersException extends RuntimeException {
        TooManySubscribersException(int max) {
            super("동시 구독자 상한(" + max + ")에 도달했습니다.");
        }
    }
}
