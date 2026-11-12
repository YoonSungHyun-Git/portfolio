package dev.ysh.eventingest;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorEventRepository extends JpaRepository<SensorEvent, Long> {
}
