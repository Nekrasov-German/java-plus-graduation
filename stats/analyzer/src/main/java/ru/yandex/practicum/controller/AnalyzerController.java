package ru.yandex.practicum.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.*;
import ru.yandex.practicum.service.AnalyzerService;

import java.util.Iterator;

@GrpcService
@RequiredArgsConstructor
public class AnalyzerController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final AnalyzerService analyzerService;

    @Override
    public void getRecommendationForUser(UserRecommendationsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            Iterator<RecommendedEventProto> iterator = analyzerService.getRecommendationForUser(request);

            while (iterator.hasNext()) {
                RecommendedEventProto event = iterator.next();
                responseObserver.onNext(event);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            Iterator<RecommendedEventProto> iterator = analyzerService.getSimilarEvents(request);

            while (iterator.hasNext()) {
                RecommendedEventProto event = iterator.next();
                responseObserver.onNext(event);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            Iterator<RecommendedEventProto> iterator = analyzerService.getInteractionsCount(request);

            while (iterator.hasNext()) {
                RecommendedEventProto event = iterator.next();
                responseObserver.onNext(event);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
