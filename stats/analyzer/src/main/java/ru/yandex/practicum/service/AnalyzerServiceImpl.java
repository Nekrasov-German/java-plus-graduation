package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserRecommendationsRequestProto;
import ru.yandex.practicum.dal.InteractionRepository;
import ru.yandex.practicum.dal.SimilarityRepository;
import ru.yandex.practicum.mappers.ActionMapper;
import ru.yandex.practicum.mappers.SimilarityMapper;
import ru.yandex.practicum.models.Interaction;
import ru.yandex.practicum.models.Similarity;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerServiceImpl implements AnalyzerService {
    private final static String TOPIC_ACTION = "stats.user-actions.v1";
    private final static String TOPIC_SIMILARITY = "stats.events-similarity.v1";

    private final InteractionRepository interactionRepository;
    private final SimilarityRepository similarityRepository;

    @KafkaListener(topics = TOPIC_ACTION, containerFactory = "userActionKafkaListenerContainerFactory")
    public void consumeUserAction(UserActionAvro message) {
        log.info("Получено сообщение {} из топика {} ", TOPIC_ACTION, message);
        interactionRepository.save(ActionMapper.mapUserActionAvroToInteraction(message));
    }

    @KafkaListener(topics = TOPIC_SIMILARITY, containerFactory = "eventSimilarityKafkaListenerContainerFactory")
    public void consumeEventSimilarity(EventSimilarityAvro message) {
        log.info("Получено сообщение {} из топика {} ", TOPIC_SIMILARITY, message);
        similarityRepository.save(SimilarityMapper.mapEventSimilarityAvroToSimilarity(message));
    }

    @Override
    public Iterator<RecommendedEventProto> getRecommendationForUser(UserRecommendationsRequestProto request) {
        log.info("Вызов метода getRecommendationForUser с запросом {}", request);
        long userId = request.getUserId();
        int maxResults = request.getMaxResults();

        List<RecommendedEventProto> recommendations = new ArrayList<>();

        try {

            List<Long> userEventIds = interactionRepository.findEventIdsByUserId(userId);

            if (userEventIds.isEmpty()) {
                log.warn("Пользователь {} не имеет истории взаимодействий", userId);
                return recommendations.iterator();
            }

            List<Similarity> allSimilarEvents = similarityRepository.findSimilarEventsForEvents(userEventIds);

            Set<Long> userInteractedEvents = new HashSet<>(userEventIds);

            Set<Long> recommendedEventIds = new HashSet<>();

            for (Similarity similarity : allSimilarEvents) {
                Long similarEventId = (userEventIds.contains(similarity.getEvent1()))
                        ? similarity.getEvent2()
                        : similarity.getEvent1();

                if (!userInteractedEvents.contains(similarEventId)) {
                    if (recommendedEventIds.add(similarEventId)) {
                        recommendations.add(RecommendedEventProto.newBuilder()
                                .setEventId(similarEventId)
                                .setScore(similarity.getSimilarity())
                                .build());
                    }
                }
            }

            recommendations.sort((a, b) -> Float.compare(b.getScore(), a.getScore()));
            if (recommendations.size() > maxResults) {
                recommendations = recommendations.subList(0, maxResults);
            }
        } catch (Exception e) {
            log.error("Ошибка при генерации рекомендаций для пользователя {}", userId, e);
        }

        return recommendations.iterator();
    }

    @Override
    public Iterator<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        log.info("Вызов метода getSimilarEvents с запросом {}", request);
        long eventId = request.getEventId();
        long userId = request.getUserId();
        int maxResults = request.getMaxResults();
        int kNearestNeighbors = 5;

        List<RecommendedEventProto> similarEvents = new ArrayList<>();

        try {
            Pageable recentInteractions = PageRequest.of(0, 20);
            List<Interaction> userRecentInteractions = interactionRepository.findTopNByUserIdOrderByTimestampDesc(
                    userId, recentInteractions
            );

            if (userRecentInteractions.isEmpty()) {
                log.warn("Пользователь {} не имеет истории взаимодействий", userId);
                return similarEvents.iterator();
            }

            List<Long> userEventIds = userRecentInteractions.stream()
                    .map(Interaction::getEventId)
                    .collect(Collectors.toList());

            Set<Long> candidateEventIds = new HashSet<>();
            for (Long userEventId : userEventIds) {
                Pageable topSimilar = PageRequest.of(0, 10);
                List<Similarity> similarToUserEvent = similarityRepository.findTopSimilarEvents(
                        userEventId, topSimilar
                );

                for (Similarity similarity : similarToUserEvent) {
                    Long similarEventId = (similarity.getEvent1().equals(userEventId))
                            ? similarity.getEvent2()
                            : similarity.getEvent1();

                    if (!userEventIds.contains(similarEventId) &&
                            similarEventId != eventId) {
                        candidateEventIds.add(similarEventId);
                    }
                }
            }

            if (candidateEventIds.isEmpty()) {
                log.warn("Не найдено кандидатов для рекомендаций для пользователя {}", userId);
                return similarEvents.iterator();
            }

            List<Long> candidates = new ArrayList<>(candidateEventIds);

            for (Long candidateEventId : candidates) {
                Pageable topK = PageRequest.of(0, kNearestNeighbors);
                List<Similarity> kNearest = similarityRepository.findKNearestNeighbors(
                        candidateEventId, userEventIds, topK
                );

                if (kNearest.isEmpty()) continue;

                float weightedSum = 0.0f;
                float similaritySum = 0.0f;

                for (Similarity neighbor : kNearest) {
                    Long neighborEventId = (neighbor.getEvent1().equals(candidateEventId))
                            ? neighbor.getEvent2()
                            : neighbor.getEvent1();

                    Float userRating = interactionRepository.findRatingByUserIdAndEventId(
                            userId, neighborEventId
                    );

                    if (userRating != null) {
                        float similarity = neighbor.getSimilarity();
                        weightedSum += userRating * similarity;
                        similaritySum += similarity;
                    }
                }

                if (similaritySum > 0) {
                    float predictedScore = weightedSum / similaritySum;
                    similarEvents.add(RecommendedEventProto.newBuilder()
                            .setEventId(candidateEventId)
                            .setScore(predictedScore)
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при генерации похожих мероприятий для пользователя {} и события {}",
                    userId, eventId, e);
        }

        similarEvents.sort((a, b) -> Float.compare(b.getScore(), a.getScore()));
        if (similarEvents.size() > maxResults) {
            similarEvents = similarEvents.subList(0, maxResults);
        }

        return similarEvents.iterator();
    }

    @Override
    public Iterator<RecommendedEventProto> getInteractionCount(InteractionsCountRequestProto request) {
        log.info("Вызов метода getInteractionCount с запросом {}", request);
        List<Long> eventIds = request.getEventIdList();

        List<RecommendedEventProto> interactionStats = new ArrayList<>();
        for (Long eventId : eventIds) {

            float interactionCount = interactionRepository.getTotalRatingByEventId(eventId);

            RecommendedEventProto event = RecommendedEventProto.newBuilder()
                    .setEventId(eventId)
                    .setScore(interactionCount)
                    .build();
            interactionStats.add(event);
        }

        return interactionStats.iterator();
    }
}
