package ru.yandex.practicum.mappers;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.models.Interaction;

@UtilityClass
public class ActionMapper {

    public Interaction mapUserActionAvroToInteraction(UserActionAvro userAction) {
        return Interaction.builder()
                .userId(userAction.getUserId())
                .eventId(userAction.getEventId())
                .rating(getRating(userAction.getActionType().toString()))
                .timestamp(userAction.getTimestamp())
                .build();
    }

    private Float getRating(String actionType) {
        try {
            ActionUser actionUser = ActionUser.valueOf(actionType);
            return actionUser.getWeight();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неизвестный тип взаимодействия: " + actionType);
        }
    }
}
