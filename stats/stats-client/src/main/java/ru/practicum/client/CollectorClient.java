package ru.practicum.client;

import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub blockingStub;

    @Async
    public CompletableFuture<Boolean> sendUserAction(Long userId, Long eventId, String actionType) {
        try {
            UserActionProto request = UserActionProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setActionType(convertToActionTypeProto(actionType))
                    .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                            .setSeconds(Instant.now().getEpochSecond())
                            .build())
                    .build();

            Empty response = blockingStub.collectUserAction(request);
            return CompletableFuture.completedFuture(true);
        } catch (StatusRuntimeException e) {
            log.error("gRPC ошибка при отправке действия пользователя", e);
            return CompletableFuture.completedFuture(false);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при отправке действия", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Конвертирует строковый тип действия в protobuf-тип
     */
    private ru.practicum.ewm.stats.proto.ActionTypeProto convertToActionTypeProto(String actionType) {
        switch (actionType.toUpperCase()) {
            case "VIEW":
                return ActionTypeProto.ACTION_VIEW;
            case "REGISTER":
                return ActionTypeProto.ACTION_REGISTER;
            case "LIKE":
                return ActionTypeProto.ACTION_LIKE;
            default:
                throw new IllegalArgumentException("Неизвестный тип действия: " + actionType);
        }
    }
}
