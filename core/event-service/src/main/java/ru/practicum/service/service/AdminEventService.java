package ru.practicum.service.service;

import ru.practicum.interaction.dto.EventFullDto;
import ru.practicum.interaction.dto.UpdateEventAdminRequest;
import ru.practicum.service.dto.AdminEventParam;

import java.util.List;

public interface AdminEventService {
    List<EventFullDto> getFullEvents(AdminEventParam params);

    EventFullDto updateEventByAdmin(Long id, UpdateEventAdminRequest request);
}