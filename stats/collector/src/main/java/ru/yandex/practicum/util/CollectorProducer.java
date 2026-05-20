package ru.yandex.practicum.util;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Service
@RequiredArgsConstructor
public class CollectorProducer {
    private final KafkaTemplate<String, UserActionAvro> producer;

    public void sendMessage(String topic, UserActionAvro message) {
        producer.send(topic, message);
    }
}
