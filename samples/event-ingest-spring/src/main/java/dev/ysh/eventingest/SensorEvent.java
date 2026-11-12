package dev.ysh.eventingest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/** 센서가 보낸 계측 이벤트 한 건. */
@Entity
@Table(name = "sensor_event")
public class SensorEvent {

    @Id
    @GeneratedValue
    private Long id;

    private String deviceCode;
    private int channelNumber;

    @Column(name = "distance_m")
    private double distanceM;

    // value는 SQL 예약어라 컬럼 이름을 바꾼다.
    @Column(name = "event_value")
    private double value;
    private OffsetDateTime eventTime;
    private OffsetDateTime receivedAt;

    protected SensorEvent() {
    }

    public SensorEvent(String deviceCode, int channelNumber, double distanceM, double value,
                       OffsetDateTime eventTime, OffsetDateTime receivedAt) {
        this.deviceCode = deviceCode;
        this.channelNumber = channelNumber;
        this.distanceM = distanceM;
        this.value = value;
        this.eventTime = eventTime;
        this.receivedAt = receivedAt;
    }

    public Long getId() { return id; }
    public String getDeviceCode() { return deviceCode; }
    public int getChannelNumber() { return channelNumber; }
    public double getDistanceM() { return distanceM; }
    public double getValue() { return value; }
    public OffsetDateTime getEventTime() { return eventTime; }
    public OffsetDateTime getReceivedAt() { return receivedAt; }
}
