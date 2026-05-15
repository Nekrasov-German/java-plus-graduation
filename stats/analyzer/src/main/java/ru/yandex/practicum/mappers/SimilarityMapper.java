package ru.yandex.practicum.mappers;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.models.Similarity;

@UtilityClass
public class SimilarityMapper {
    public Similarity mapEventSimilarityAvroToSimilarity(EventSimilarityAvro similarityAvro) {
        float score = (float) similarityAvro.getScore();

        return Similarity.builder()
                .event1(similarityAvro.getEventA())
                .event2(similarityAvro.getEventB())
                .similarity(score)
                .timestamp(similarityAvro.getTimestamp())
                .build();
    }
}
