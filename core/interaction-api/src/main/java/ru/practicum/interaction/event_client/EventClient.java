package ru.practicum.interaction.event_client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.dto.EventFullDto;
import ru.practicum.interaction.dto.UpdateEventUserRequest;

@FeignClient(name = "event-service")
public interface EventClient {

    @GetMapping("/users/{userId}/events/{eventId}")
    ResponseEntity<EventFullDto> getInfoEvent(@PathVariable(value = "userId") Long userId,
                                              @PathVariable(value = "eventId") Long eventId);

    @PostMapping("/users/{userId}/events/{eventId}")
    ResponseEntity<EventFullDto> updateRequestEvent(@PathVariable(value = "userId") Long userId,
                                             @PathVariable(value = "eventId") Long eventId,
                                             @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest);

    @PostMapping("/users/{userId}/events/{eventId}/confirmed/{confirmed}")
    ResponseEntity<EventFullDto> updateConfirmedRequest(@PathVariable(value = "userId") Long userId,
                                                        @PathVariable(value = "eventId") Long eventId,
                                                        @PathVariable(value = "confirmed") Long confirmed);
}
