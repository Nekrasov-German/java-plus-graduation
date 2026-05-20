package ru.practicum.requests.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.CollectorClient;
import ru.practicum.interaction.dto.*;
import ru.practicum.interaction.dto.enums.State;
import ru.practicum.interaction.dto.enums.Status;
import ru.practicum.interaction.event_client.EventClient;
import ru.practicum.interaction.user_client.UserClient;
import ru.practicum.requests.dal.RequestRepository;
import ru.practicum.requests.exceptions.ConflictException;
import ru.practicum.requests.exceptions.NotFoundException;
import ru.practicum.requests.exceptions.ValidationException;
import ru.practicum.requests.mapper.RequestMapper;
import ru.practicum.requests.model.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrivateRequestServiceImpl implements PrivateRequestService {
    private static final String REGISTER = "REGISTER";

    private static final Logger log = LoggerFactory.getLogger(PrivateRequestServiceImpl.class);
    private final UserClient userClient;
    private final EventClient eventClient;
    private final CollectorClient client;
    private final RequestRepository requestRepository;

    @Override
    public List<ParticipationRequestDto> getInfoRequest(Long userId, Long eventId) {
        log.info("Получение информации на участие в событии {} user {}", eventId, userId );
        try {
            eventClient.getInfoEvent(userId, eventId);
        } catch (FeignException e) {
            throw new ValidationException("Событие не найдено");
        }

        try {
            userClient.getUser(userId);
        } catch (FeignException e) {
            throw new ValidationException("Пользователь не найден");
        }

        return requestRepository.findAllByEvent(eventId).stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        try {
            userClient.getUser(userId);
        } catch (FeignException e) {
            throw new ValidationException("Пользователь не найден");
        }

        EventFullDto event;
        try {
            event = eventClient.getInfoEvent(userId, eventId).getBody();
        } catch (FeignException e) {
            throw new ValidationException("Событие не найдено");
        }

        if (!event.getInitiatorDto().getId().equals(userId)) {
            throw new ConflictException("Только владелец может обновить статус запроса");
        }

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Подтверждение заявок не требуется");
        }

        List<Long> userIds = updateRequest.getRequestIds();
        Status status = updateRequest.getStatus();

        List<Request> requests = requestRepository.findAllById(userIds);

        for (Request request : requests) {
            if (request.getStatus() != Status.PENDING) {
                throw new ConflictException("Статус заявки ID=" + request.getId() +
                        " нельзя изменить: текущий статус — " + request.getStatus());
            }
        }

        Map<Long, Request> requestMap = requests.stream()
                .collect(Collectors.toMap(Request::getId, request -> request));

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        if (status.equals(Status.CONFIRMED)) {
            long confirmed = event.getConfirmedRequests();
            long limit = event.getParticipantLimit();

            if (limit > 0 && confirmed >= limit) {
                throw new ConflictException("Достигнут лимит участников события");
            }

            long availableSlots = (limit == 0) ? requests.size() : limit - confirmed;

            if (availableSlots <= 0) {
                for (Long id : userIds) {
                    Request request = requestMap.get(id);
                    if (request != null) {
                        request.setStatus(Status.REJECTED);
                        rejectedRequests.add(RequestMapper.toRequestDto(request));
                    }
                }
            } else {
                int confirmedCount = 0;

                for (Long id : userIds) {
                    Request request = requestMap.get(id);
                    if (request == null) continue;

                    if (confirmedCount < availableSlots) {
                        request.setStatus(Status.CONFIRMED);
                        confirmedRequests.add(RequestMapper.toRequestDto(request));
                        confirmedCount++;
                    } else {
                        request.setStatus(Status.REJECTED);
                        rejectedRequests.add(RequestMapper.toRequestDto(request));
                    }
                }

                Long result = confirmed + confirmedCount;

                try {
                    eventClient.updateConfirmedRequest(userId, eventId, result);
                    log.info("Обновлено количество одобренных заявок. {}", result);
                } catch (FeignException e) {
                    throw new ConflictException("Не удалось обновить событие. " + e);
                }

            }

        } else if (status.equals(Status.REJECTED)) {

            for (Long requestId : userIds) {
                Request request = requestMap.get(requestId);

                if (request != null) {
                    request.setStatus(Status.REJECTED);
                    rejectedRequests.add(RequestMapper.toRequestDto(request));
                }
            }

        } else {
            throw new ConflictException("Недопустимый статус для обновления: " + status);
        }

        requestRepository.saveAll(requests);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    @Override
    public List<ParticipationRequestDto> getInfoOnParticipation(Long userId) {

        log.info("GET INFO PARTICIPATION {}", userId);
        try {
            userClient.getShortUser(userId);
        } catch (FeignException e) {
            throw new ValidationException("Пользователь не найден");
        }

        return requestRepository.findAllByRequester(userId).stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequestForParticipation(Long userId, Long eventId) {
        log.info("CREATE REQUEST FOR PARTICIPATION");
        UserShortDto user;
        try {
            user = userClient.getShortUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Пользователь не найден");
        }

        EventFullDto event;
        try {
            event = eventClient.getInfoEvent(userId, eventId).getBody();
        } catch (FeignException e) {
            throw new NotFoundException("Такого события или пользователя не найдено.");
        }

        if (user.getId() == event.getInitiatorDto().getId()) {
            throw new ConflictException("инициатор события не может добавить запрос на участие в своём событии.");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("нельзя участвовать в неопубликованном событии.");
        }

        long confirmedRequest = event.getConfirmedRequests();
        log.info("CONFIRMED : {} LIMIT : {}", confirmedRequest, event.getParticipantLimit());

        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= confirmedRequest) {
            throw new ConflictException("у события достигнут лимит запросов на участие");
        }

        Optional<Request> existingRequest = requestRepository.findByEventAndRequester(eventId, userId);
        if (existingRequest.isPresent()) {
            throw new ConflictException("Пользователь уже подал запрос на участие в этом событии.");
        }

        Request request = Request.builder()
                .requester(userId)
                .event(eventId)
                .build();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(Status.CONFIRMED);
            confirmedRequest = confirmedRequest + 1;
            try {
                eventClient.updateConfirmedRequest(userId, eventId, confirmedRequest);
                log.info("Добавлено количество одобренных заявок. {}", confirmedRequest);
            } catch (FeignException e) {
                throw new ConflictException("Не удалось обновить событие. " + e);
            }
        }

        client.sendUserAction(userId, eventId, REGISTER)
                .exceptionally(ex -> {
                    log.warn("Асинхронная отправка статистики не удалась", ex);
                    return false;
                });

        return RequestMapper.toRequestDto(requestRepository.save(request));

    }

    @Override
    @Transactional
    public ParticipationRequestDto canceledRequestForParticipation(Long userId, Long requestId) {
        log.info("CANCELED PARTICIPATION {} request {}", userId, requestId);

        UserShortDto user;
        try {
            user = userClient.getShortUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Пользователь не найден");
        }

        Optional<Request> request = requestRepository.findById(requestId);
        if (request.isPresent()) {
            Request update = request.get();
            update.setStatus(Status.CANCELED);
            requestRepository.save(update);
        } else {
            throw new NotFoundException("Такого запроса нет.");
        }
        return RequestMapper.toRequestDto(request.get());
    }
}
