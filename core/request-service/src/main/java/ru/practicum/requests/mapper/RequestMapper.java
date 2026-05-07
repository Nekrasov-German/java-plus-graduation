package ru.practicum.requests.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.interaction.dto.ParticipationRequestDto;
import ru.practicum.requests.model.Request;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@UtilityClass
public class RequestMapper {
    public ParticipationRequestDto toRequestDto(Request request) {
        LocalDateTime created = request.getCreated()
                .truncatedTo(ChronoUnit.MILLIS);

        return ParticipationRequestDto.builder()
                .requesterId(request.getRequester())
                .eventId(request.getEvent())
                .created(created)
                .status(request.getStatus())
                .id(request.getId())
                .build();
    }
}
