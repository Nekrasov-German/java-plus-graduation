package ru.practicum.service.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.interaction.dto.EventFullDto;
import ru.practicum.interaction.dto.EventShortDto;
import ru.practicum.interaction.dto.EventSort;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicEventService {
    List<EventShortDto> getEvents(String text,
                                  List<Long> categories,
                                  Boolean paid,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  Boolean onlyAvailable,
                                  EventSort sort,
                                  Integer from,
                                  Integer size,
                                  HttpServletRequest request);

    EventFullDto getById(Long id, HttpServletRequest request);
}
