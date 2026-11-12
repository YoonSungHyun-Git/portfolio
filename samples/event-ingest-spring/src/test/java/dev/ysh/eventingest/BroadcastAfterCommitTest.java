package dev.ysh.eventingest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/** 알림은 커밋된 이벤트에만 나가야 한다. 롤백된 이벤트가 화면에 먼저 뜨면 안 된다. */
@SpringBootTest
class BroadcastAfterCommitTest {

    @Autowired
    SensorEventService service;

    @Autowired
    TransactionTemplate tx;

    @SpyBean
    EventStreamService stream;

    private static EventDtos.CreateRequest request() {
        return new EventDtos.CreateRequest("DAS", 2, 120.0, 1.5, OffsetDateTime.now().minusSeconds(1));
    }

    @Test
    void 커밋되면_한_번_알린다() {
        service.create(request());
        verify(stream, times(1)).broadcast(any());
    }

    @Test
    void 롤백되면_알리지_않는다() {
        tx.executeWithoutResult(status -> {
            service.create(request());
            status.setRollbackOnly();
        });
        verify(stream, never()).broadcast(any());
    }
}
