package ru.yandex.practicum.service;

import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserRecommendationsRequestProto;

import java.util.Iterator;

public interface AnalyzerService {

    Iterator<RecommendedEventProto> getRecommendationForUser(UserRecommendationsRequestProto request);

    Iterator<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request);

    Iterator<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request);

}
