package dev.ysh.eventingest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final SensorEventService events;
    private final EventStreamService stream;

    public EventController(SensorEventService events, EventStreamService stream) {
        this.events = events;
        this.stream = stream;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDtos.Response create(@Valid @RequestBody EventDtos.CreateRequest request) {
        return events.create(request);
    }

    @GetMapping
    public List<EventDtos.Response> list() {
        return events.recent();
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return stream.subscribe();
    }
}
