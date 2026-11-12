package dev.ysh.eventingest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class SensorEventService {

    private final SensorEventRepository repository;
    private final EventStreamService stream;
    private final Clock clock;

    public SensorEventService(SensorEventRepository repository, EventStreamService stream, Clock clock) {
        this.repository = repository;
        this.stream = stream;
        this.clock = clock;
    }

    /**
     * 저장 후 구독자에게 알린다. 알림은 커밋이 끝난 뒤에 보낸다.
     * 커밋 전에 보내면, 롤백된 이벤트를 화면이 먼저 보여주는 일이 생길 수 있다.
     */
    @Transactional
    public EventDtos.Response create(EventDtos.CreateRequest req) {
        SensorEvent saved = repository.save(new SensorEvent(
                req.deviceCode(), req.channelNumber(), req.distanceM(), req.value(),
                req.eventTime(), OffsetDateTime.now(clock)));
        EventDtos.Response response = EventDtos.Response.from(saved);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                stream.broadcast(response);
            }
        });
        return response;
    }

    @Transactional(readOnly = true)
    public List<EventDtos.Response> recent() {
        return repository.findAll().stream()
                .map(EventDtos.Response::from)
                .toList();
    }
}
