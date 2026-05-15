package ru.practicum.service.service;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.client.AnalyzerClient;
import ru.practicum.client.CollectorClient;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.interaction.dto.EventFullDto;
import ru.practicum.interaction.dto.EventShortDto;
import ru.practicum.interaction.dto.EventSort;
import ru.practicum.interaction.dto.UserShortDto;
import ru.practicum.interaction.user_client.UserClient;
import ru.practicum.service.dal.EventRepository;
import ru.practicum.service.error.NotFoundException;
import ru.practicum.service.error.ValidationException;
import ru.practicum.service.mapper.EventMapper;
import ru.practicum.service.model.Event;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class PublicEventServiceImpl implements PublicEventService {
    static final String VIEW = "VIEW";
    static final int MAX_RESULT = 20;
    static final String LIKE = "LIKE";

    final EventRepository eventRepository;
    final UserClient userClient;
    final AnalyzerClient analyzerClient;
    final CollectorClient client;
    static final String URI_EVENT_ENDPOINT = "/events/";

    @Override
    public List<EventShortDto> getEvents(String text,
                                         List<Long> categories,
                                         Boolean paid,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         Boolean onlyAvailable,
                                         EventSort sort,
                                         Integer from,
                                         Integer size,
                                         HttpServletRequest request) {
        if (rangeStart == null && rangeEnd == null) rangeStart = LocalDateTime.now();
        if (rangeEnd == null) rangeEnd = LocalDateTime.now().plusYears(1000);

        log.info("PublicEventService: Поиск ивентов с заданными параметрами");
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> eventsList = eventRepository.findPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageable);
        log.info("PublicEventService: {}", eventsList);

        List<EventShortDto> result = eventsList.stream()
                .map(event -> EventMapper.toEventShortDto(event, 0.0))
                .toList();

        if (sort == EventSort.VIEWS) result = result.stream()
                .sorted(Comparator.comparingDouble(EventShortDto::getRating)
                        .reversed()).toList();

        return result;
    }

    @Override
    public EventFullDto getById(Long id, HttpServletRequest request, Long userId) {
        log.info("PublicEventService: Поиск ивента с переданным id: {}", id);
        Event event = eventRepository.findPublishedById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id: %d не найдено", id)));

        UserShortDto user;
        try {
            user = userClient.getShortUser(event.getInitiator());
        } catch (FeignException e) {
            throw new NotFoundException("Пользователь не найдено.");
        }

        client.sendUserAction(userId, id, VIEW)
                .exceptionally(ex -> {
                    log.warn("Асинхронная отправка статистики не удалась", ex);
                    return false;
                });

        Double views = 0.0;
		return EventMapper.toEventFullDto(event, views, user);
    }

    @Override
    public List<EventShortDto> getRecommendationEvent(Long userId) {
        try {
            userClient.getShortUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Такого пользователя не существует.");
        }

        try {
            Stream<RecommendedEventProto> recommendationStream =
                    analyzerClient.getRecommendationForUser(userId, MAX_RESULT);

            Set<Long> eventIds = recommendationStream.map(RecommendedEventProto::getEventId).collect(Collectors.toSet());

            List<Event> events = eventRepository.findAllByIdIn(eventIds);

            return events.stream().map(EventMapper::toEventShortDto).toList();
        } catch (RuntimeException e) {
            log.info("Ошибка получения рекомендаций");
            return List.of();
        }
    }

    @Override
    public void likeEvent(Long eventId, Long userId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Такого события не найдено."));

        try {
            userClient.getShortUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Такого пользователя не существует.");
        }

        Stream<RecommendedEventProto> recommendationStream = Stream.empty();

        try {
            recommendationStream =
                    analyzerClient.getSimilarEvent(eventId, userId, MAX_RESULT);
        } catch (RuntimeException e) {
            log.info("Ошибка получения рекомендаций с которыми взаимодействовал пользователь");
        }

        Set<Long> eventIds = recommendationStream.map(RecommendedEventProto::getEventId).collect(Collectors.toSet());

        if (eventIds.isEmpty()) {
            throw new ValidationException("Пользователь не взаимодействовал с мероприятием");
        }

        client.sendUserAction(userId, eventId, LIKE)
                .exceptionally(ex -> {
                    log.warn("Асинхронная отправка статистики не удалась", ex);
                    return false;
                });
    }
}
