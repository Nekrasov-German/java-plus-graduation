package ru.practicum.requests.service;

import ru.practicum.interaction.dto.EventRequestStatusUpdateRequest;
import ru.practicum.interaction.dto.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.ParticipationRequestDto;

import java.util.List;

public interface PrivateRequestService {
    List<ParticipationRequestDto> getInfoRequest(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest updateRequest);

    List<ParticipationRequestDto> getInfoOnParticipation(Long userId);

    ParticipationRequestDto createRequestForParticipation(Long userId, Long eventId);

    ParticipationRequestDto canceledRequestForParticipation(Long userId, Long requestId);
}
