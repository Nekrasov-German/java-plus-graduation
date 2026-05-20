package ru.yandex.practicum.util.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@UtilityClass
public class MapperUserAction {
    public UserActionAvro userActionProtoToUserActionAvro(UserActionProto userAction) {
        ActionTypeProto type = userAction.getActionType();

        ActionTypeAvro typeAvro = null;

        switch (type) {
            case ACTION_LIKE -> typeAvro = ActionTypeAvro.LIKE;
            case ACTION_VIEW -> typeAvro = ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> typeAvro = ActionTypeAvro.REGISTER;
        }

        return UserActionAvro.newBuilder()
                .setUserId(userAction.getUserId())
                .setEventId(userAction.getEventId())
                .setActionType(typeAvro)
                .setTimestamp(Instant.ofEpochSecond(userAction.getTimestamp().getSeconds(),
                        userAction.getTimestamp().getNanos()))
                .build();
    }
}
