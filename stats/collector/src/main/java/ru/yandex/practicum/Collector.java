package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Instant;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Collector {
    public static void main(String[] args) {
        SpringApplication.run(Collector.class, args);
    }
}

