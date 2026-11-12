package dev.ysh.eventingest;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventStreamServiceTest {

    @Test
    void 구독자_상한을_넘으면_거절한다() {
        EventStreamService service = new EventStreamService(new EventStreamProperties(2, 60_000));
        service.subscribe();
        service.subscribe();

        assertThatThrownBy(service::subscribe)
                .isInstanceOf(EventStreamService.TooManySubscribersException.class);
        assertThat(service.subscriberCount()).isEqualTo(2);
    }

    @Test
    void 완료된_연결로의_전송은_실패하고_구독에서_빠진다() {
        EventStreamService service = new EventStreamService(new EventStreamProperties(10, 60_000));
        service.subscribe().complete();

        service.broadcast(new EventDtos.Response(1, "DAS", 1, 10.0, 0.5,
                OffsetDateTime.now(), OffsetDateTime.now()));

        assertThat(service.subscriberCount()).isZero();
    }
}
