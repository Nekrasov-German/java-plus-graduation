package ru.practicum.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.*;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class AnalyzerClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub blockingStub;

    public Stream<RecommendedEventProto> getRecommendationForUser(long userId, int maxResults) {
        UserRecommendationsRequestProto request = UserRecommendationsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        Iterator<RecommendedEventProto> iterator = blockingStub.getRecommendationForUser(request);

        return asStream(iterator);
    }
    // Синхронный вызов
    public Stream<RecommendedEventProto> getSimilarEvent(long eventId, long userId, int maxResults) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        Iterator<RecommendedEventProto> iterator = blockingStub.getSimilarEvents(request);

        return asStream(iterator);
    }

    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventsId) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventsId)
                .build();

        Iterator<RecommendedEventProto> iterator = blockingStub.getInteractionsCount(request);

        return asStream(iterator);
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
