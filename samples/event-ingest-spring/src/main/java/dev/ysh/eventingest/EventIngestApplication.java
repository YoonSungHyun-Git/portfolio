package dev.ysh.eventingest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EventIngestApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventIngestApplication.class, args);
    }
}
