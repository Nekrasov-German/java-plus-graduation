package ru.practicum.requests.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.requests.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository  extends JpaRepository<Request, Long> {

    List<Request> findAllByEvent(Long eventId);

    List<Request> findAllByRequester(Long userId);

    Optional<Request> findByEventAndRequester(Long eventId, Long userId);

}
