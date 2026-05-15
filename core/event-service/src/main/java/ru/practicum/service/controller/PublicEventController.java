package ru.practicum.service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.dto.EventFullDto;
import ru.practicum.interaction.dto.EventSearchParams;
import ru.practicum.interaction.dto.EventShortDto;
import ru.practicum.service.service.PublicEventService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/events")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class PublicEventController {
    final PublicEventService publicEventService;

    @GetMapping
    public List<EventShortDto> getEvents(@Valid EventSearchParams params,
                                         HttpServletRequest request) {
        log.info("PublicEventController: вызов эндпоинта GET events/ " +
                 "с параметрами запроса --  " +
                 "text:{}, categories:{}, paid:{}, rangeStart:{}, rangeEnd:{}, onlyAvailable:{}, sort:{}, from:{}, size:{}",
                params.getText(), params.getCategories(), params.getPaid(), params.getRangeStart(), params.getRangeEnd(),
                params.getOnlyAvailable(), params.getSort(), params.getFrom(), params.getSize());

        return publicEventService.getEvents(params.getText(),
                params.getCategories(),
                params.getPaid(),
                params.getRangeStart(),
                params.getRangeEnd(),
                params.getOnlyAvailable(),
                params.getSort(),
                params.getFrom(),
                params.getSize(),
                request);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable(value = "id") Long id, HttpServletRequest request,
                                     @RequestHeader("X-EWM-USER-ID") long userId) {
        log.info("PublicEventController: вызов эндпоинта GET events/{}", id);
        return publicEventService.getById(id, request, userId);
    }

    @GetMapping("/recommendations")
    public List<EventShortDto> getRecommendationEvents(@RequestHeader("X-EWM-USER-ID") long userId) {
        return publicEventService.getRecommendationEvent(userId);
    }

    @PutMapping("/{eventId}/like")
    public ResponseEntity<Void> likeEvent(@PathVariable(value = "eventId") Long eventId,
                                          @RequestHeader("X-EWM-USER-ID") long userId) {
        publicEventService.likeEvent(eventId, userId);
        return ResponseEntity.ok().body(null);
    }
}
