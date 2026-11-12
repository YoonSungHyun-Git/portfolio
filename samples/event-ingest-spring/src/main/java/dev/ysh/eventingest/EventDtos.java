package dev.ysh.eventingest;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;

import java.time.OffsetDateTime;

public final class EventDtos {

    private EventDtos() {
    }

    /**
     * 장비가 보내는 이벤트 요청. 잘못된 값은 컨트롤러 입구에서 400으로 돌려보내고 저장하지 않는다.
     * value를 double이 아닌 Double로 받는 이유: 필드가 빠졌을 때 0.0으로 조용히 들어가는 것을 막기 위해.
     */
    public record CreateRequest(
            @NotNull @Pattern(regexp = "[A-Z]{2,10}") String deviceCode,
            @NotNull @Min(1) @Max(4) Integer channelNumber,
            @NotNull @DecimalMin("0.0") Double distanceM,
            @NotNull Double value,
            @NotNull @PastOrPresent OffsetDateTime eventTime
    ) {
    }

    public record Response(
            long id,
            String deviceCode,
            int channelNumber,
            double distanceM,
            double value,
            OffsetDateTime eventTime,
            OffsetDateTime receivedAt
    ) {
        static Response from(SensorEvent e) {
            return new Response(e.getId(), e.getDeviceCode(), e.getChannelNumber(), e.getDistanceM(),
                    e.getValue(), e.getEventTime(), e.getReceivedAt());
        }
    }
}
