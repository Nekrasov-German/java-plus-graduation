package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.util.AggregatorProducer;

import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorService {
    private final static String TOPIC_ACTION = "stats.user-actions.v1";
    private final static String TOPIC_SIMILARITY = "stats.events-similarity.v1";
    private final static double VIEW = 0.4;
    private final static double REGISTER = 0.8;
    private final static double LIKE = 1;

    private final AggregatorProducer producer;

    private final Map<Long, Map<Long, Double>> similarityEvent = new HashMap<>();
    private final Map<Long, Map<Long, Double>> eventRating = new HashMap<>();

    @KafkaListener(topics = TOPIC_ACTION, groupId = "my-group")
    public void consumeMessage(UserActionAvro message) {
        log.info("Получено сообщение из топика : {}", TOPIC_ACTION);
        log.info(message.toString());
        try {
            calculateWeight(message);
        } catch (Exception e) {
            log.info("Ошибка сервиса");
        }
    }

    private void calculateWeight(UserActionAvro action) {

        boolean updateEventRating = calculateWeight(action.getEventId(), action.getUserId(), getWeight(action));

        if (updateEventRating) {
            log.info("Положили событие в мапу.");
            Set<Long> events = eventRating.keySet();
            if (!events.isEmpty()) {
                for (Long eventId : events) {
                    if (eventId != action.getEventId()) {
                        log.info("Сравниваем два события {} - {}", eventId, action.getEventId());
                        Map<Long, Double> users = eventRating.get(eventId);
                        if (users.containsKey(action.getUserId())) {
                            log.info("Пользователь {} взаимодействовал с обоими событиями.", action.getUserId());
                            calculateSimilarity(action.getEventId(), eventId);
                        }
                    }
                }
            }
        }
    }

    private double getWeight(UserActionAvro action) {
        switch (action.getActionType().toString()) {
            case "VIEW" -> {
                return VIEW;
            }
            case "REGISTER" -> {
                return REGISTER;
            }
            case "LIKE" -> {
                return LIKE;
            }
            default -> {
                return 0;
            }
        }
    }

    private boolean calculateWeight(Long eventId, Long userId, Double newWeight) {
        Map<Long, Double> userWeights = eventRating.computeIfAbsent(eventId, k -> new HashMap<>());

        Double oldWeight = userWeights.get(userId);
        if (oldWeight == null || newWeight > oldWeight) {
            userWeights.put(userId, newWeight);
            log.info("Updated/Created weight for event {} user {}", eventId, userId);
            return true;
        }
        return false;
    }

    private void calculateSimilarity(Long eventA, Long eventB) {
        Map<Long, Double> usersEventA = eventRating.get(eventA);
        Map<Long, Double> usersEventB = eventRating.get(eventB);

        if (usersEventA == null || usersEventB == null) return;

        double chislitel = 0.0;
        double sumUsersA = 0.0;
        double sumUsersB = 0.0;

        Set<Long> allUsers = new HashSet<>(usersEventA.keySet());
        allUsers.addAll(usersEventB.keySet());

        for (Long userId : allUsers) {
            Double weightA = usersEventA.get(userId);
            Double weightB = usersEventB.get(userId);

            if (weightA != null) sumUsersA += weightA;
            if (weightB != null) sumUsersB += weightB;

            if (weightA != null && weightB != null) {
                chislitel += Math.min(weightA, weightB);
            }
        }

        if (sumUsersA == 0 || sumUsersB == 0) return;

        double similarity = chislitel / (Math.sqrt(sumUsersA) * Math.sqrt(sumUsersB));

        if (eventA > eventB) {
            Long temp = eventA;
            eventA = eventB;
            eventB = temp;
        }

        if (similarityEvent.containsKey(eventA) && similarityEvent.get(eventA).containsKey(eventB)) {
            log.info("Если запись в мапе существует");
            Double oldSimilarity = similarityEvent.get(eventA).get(eventB);
            log.info("Сравниваем старое {} новое {}", oldSimilarity, similarity);
            if (Math.abs(oldSimilarity - similarity) > 1e-9) {
                log.info("Добавили значение в мапу");
                similarityEvent.get(eventA).put(eventB, similarity);
                log.info("Добавили значение в мапу {}", similarityEvent.get(eventA));
                try {
                    EventSimilarityAvro avroMessage = EventSimilarityAvro.newBuilder()
                            .setEventA(eventA)
                            .setEventB(eventB)
                            .setScore(similarity)
                            .setTimestamp(Instant.now())
                            .build();
                    producer.sendMessage(TOPIC_SIMILARITY, avroMessage);
                    log.debug("Сообщение отправлено в топик: {}", TOPIC_SIMILARITY);
                } catch (Exception e) {
                    log.error("Ошибка отправки в Kafka ", e);
                }
            }
        } else {
            Map<Long, Double> similarityMap = new HashMap<>();
            similarityMap.put(eventB, similarity);
            similarityEvent.put(eventA, similarityMap);
            log.info("Создали новую запись {}", similarityEvent.get(eventA));
            try {
                EventSimilarityAvro avroMessage = EventSimilarityAvro.newBuilder()
                        .setEventA(eventA)
                        .setEventB(eventB)
                        .setScore(similarity)
                        .setTimestamp(Instant.now())
                        .build();
                producer.sendMessage(TOPIC_SIMILARITY, avroMessage);
                log.debug("Сообщение отправлено в топик: {}", TOPIC_SIMILARITY);
            } catch (Exception e) {
                log.error("Ошибка отправки в Kafka ", e);
            }
        }
    }
}
