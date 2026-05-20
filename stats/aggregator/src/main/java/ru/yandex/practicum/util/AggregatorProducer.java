package ru.yandex.practicum.util;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Service
@RequiredArgsConstructor
public class AggregatorProducer {

    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    public void sendMessage(String topic, EventSimilarityAvro message) {
        kafkaTemplate.send(topic, message);
    }
}
