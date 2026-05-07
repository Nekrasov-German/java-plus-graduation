package ru.practicum.requests.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.dto.EventRequestStatusUpdateRequest;
import ru.practicum.interaction.dto.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.ParticipationRequestDto;
import ru.practicum.requests.service.PrivateRequestService;

import java.util.List;

@Slf4j
@Controller
@Validated
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class PrivateRequestsController {
    private final PrivateRequestService service;

    @GetMapping("/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getInfoOnParticipation(
            @PathVariable(value = "userId") Long userId) {
        log.info("создание запроса.");
        return ResponseEntity.ok().body(service.getInfoOnParticipation(userId));
    }

    @PostMapping("/requests")
    public ResponseEntity<ParticipationRequestDto> createRequestForParticipation(
            @PathVariable(value = "userId") Long userId,
            @RequestParam(value = "eventId", required = true) Long eventId) {
        log.info("создание запроса.");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createRequestForParticipation(userId, eventId));
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> canceledRequestForParticipation(
            @PathVariable(value = "userId") Long userId,
            @PathVariable(value = "requestId") Long requestId) {
        return ResponseEntity.ok().body(service.canceledRequestForParticipation(userId, requestId));
    }

    @GetMapping("/events/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getInfoRequest(@PathVariable(value = "userId") Long userId,
                                                                        @PathVariable(value = "eventId") Long eventId) {
        return ResponseEntity.ok().body(service.getInfoRequest(userId, eventId));
    }

    @PatchMapping("/events/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateStatusRequest(
            @PathVariable(value = "userId") Long userId,
            @PathVariable(value = "eventId") Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        return ResponseEntity.ok().body(service.updateStatusRequest(userId, eventId, updateRequest));
    }
}
