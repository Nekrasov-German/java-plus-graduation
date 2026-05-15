package ru.yandex.practicum.mappers;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.models.Interaction;

@UtilityClass
public class ActionMapper {
    private final float VIEW = 0.4F;
    private final float REGISTER = 0.8F;
    private final float LIKE = 1.0F;

    public Interaction mapUserActionAvroToInteraction(UserActionAvro userAction) {
        return Interaction.builder()
                .userId(userAction.getUserId())
                .eventId(userAction.getEventId())
                .rating(getRating(userAction.getActionType().toString()))
                .timestamp(userAction.getTimestamp())
                .build();
    }

    private Float getRating(String actionType) {
        switch (actionType) {
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
                return 0.0F;
            }
        }
    }
}
