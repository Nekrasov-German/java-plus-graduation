package ru.practicum.service.service;

import ru.practicum.interaction.dto.*;

import java.util.List;

public interface PrivateService {

    List<EventShortDto> getEventsByOwner(Long userId, Long from, Long size);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getInfoEvent(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventFullDto updateForRequestEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventFullDto updateConfirmedRequests(Long userId, Long eventId, Long confirmed);

}
