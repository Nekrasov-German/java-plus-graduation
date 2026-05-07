package ru.practicum.service.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatClient;
import ru.practicum.dto.response.HitsCounterResponseDto;
import ru.practicum.interaction.dto.*;
import ru.practicum.interaction.dto.enums.State;
import ru.practicum.interaction.user_client.UserClient;
import ru.practicum.service.dal.CategoryRepository;
import ru.practicum.service.dal.EventRepository;
import ru.practicum.service.error.ConflictException;
import ru.practicum.service.error.NotFoundException;
import ru.practicum.service.error.ValidationException;
import ru.practicum.service.mapper.EventMapper;
import ru.practicum.service.model.Category;
import ru.practicum.service.model.Event;
import ru.practicum.service.statistics.StatisticsService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrivateServiceImpl implements PrivateService {
    private static final String URI_EVENT_ENDPOINT = "/events/";

    private final StatClient client;
    private final StatisticsService statsService;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final UserClient userClient;

    private Map<String, Long> getViewsForEvents(List<EventShortDto> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> uris = events.stream()
                .map(dto -> URI_EVENT_ENDPOINT + dto.getId())
                .collect(Collectors.toList());
        return statsService.getViewsByUris(uris, false);
    }

    @Override
    public List<EventShortDto> getEventsByOwner(Long userId, Long from, Long size) {
        int page = from.intValue() / size.intValue();
        Pageable pageable = PageRequest.of(page, size.intValue());

        Page<Event> eventPage = eventRepository.findByInitiator(userId, pageable);
        List<EventShortDto> dtos = eventPage.getContent().stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        Map<String, Long> viewsMap = getViewsForEvents(dtos);

        dtos.forEach(dto -> {
            String uriKey = URI_EVENT_ENDPOINT + dto.getId();
            Long views = viewsMap.getOrDefault(uriKey, 0L);
            dto.setViews(views);
        });

        return dtos;
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Время события должно быть за два часа до события.");
        }

        Category category = categoryRepository.findById(newEventDto.getCategoryId())
                .orElseThrow(() -> new ValidationException("Категория не указана"));

        UserDto user;
        try {
            user = userClient.getUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Такого пользователя не существует.");
        }

        Event event = eventRepository.save(EventMapper.newEventDtoToEvent(newEventDto, user, category));

        UserShortDto userShort;
        try {
            userShort = userClient.getShortUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Такого пользователя не существует.");
        }

        return EventMapper.eventToEventFullDto(event, userShort);
    }

    @Override
    public EventFullDto getInfoEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Такого события не найдено."));

        try {
            userClient.getShortUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Такого пользователя не существует.");
        }

        UserShortDto userInitiator;
        try {
            userInitiator = userClient.getShortUser(event.getInitiator());
        } catch (FeignException e) {
            throw new NotFoundException("Такого пользователя не существует.");
        }

        EventFullDto eventFullDto = EventMapper.eventToEventFullDto(event, userInitiator);

        List<HitsCounterResponseDto> hitsCounter = client.getHits(
                List.of(URI_EVENT_ENDPOINT + eventFullDto.getId()),
                true);
        Long views = hitsCounter.isEmpty() ? 0L : hitsCounter.getFirst().getHits();

        eventFullDto.setViews(views);

        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Такого события не найдено."));

        UserShortDto user;
        try {
            user = userClient.getShortUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Такого пользователя не существует.");
        }

        if (event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Данное событие нельзя изменить.");
        }
        Optional<Category> category = Optional.empty();
        if (updateEventUserRequest.getCategoryId() != null) {
            category = categoryRepository.findById(updateEventUserRequest.getCategoryId());
        }
        Event updateEvent = eventRepository
                .save(EventMapper.updateEventDtoToEvent(event, updateEventUserRequest, category));

        return EventMapper.eventToEventFullDto(updateEvent, user);
    }

    @Override
    @Transactional
    public EventFullDto updateForRequestEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Такого события не найдено."));

        UserShortDto user;
        try {
            user = userClient.getShortUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Такого пользователя не существует.");
        }

        Optional<Category> category = Optional.empty();
        if (updateEventUserRequest.getCategoryId() != null) {
            category = categoryRepository.findById(updateEventUserRequest.getCategoryId());
        }
        Event updateEvent = eventRepository
                .save(EventMapper.updateEventDtoToEvent(event, updateEventUserRequest, category));

        return EventMapper.eventToEventFullDto(updateEvent, user);
    }

    @Override
    @Transactional
    public EventFullDto updateConfirmedRequests(Long userId, Long eventId, Long confirmed) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Такого события не найдено."));
        UserShortDto user;
        try {
            user = userClient.getShortUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Такого пользователя не существует.");
        }

        event.setConfirmedRequests(confirmed);

        Event updateEvent = eventRepository.save(event);

        return EventMapper.eventToEventFullDto(updateEvent, user);
    }
}
