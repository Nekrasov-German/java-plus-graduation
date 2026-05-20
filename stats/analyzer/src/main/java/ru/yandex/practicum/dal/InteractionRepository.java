package ru.yandex.practicum.dal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.models.Interaction;

import java.util.List;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    List<Interaction> findAllByEventIdIn(List<Long> eventIds);

    // Получить все event_id, с которыми взаимодействовал пользователь
    @Query("SELECT DISTINCT i.eventId FROM Interaction i WHERE i.userId = :userId")
    List<Long> findEventIdsByUserId(@Param("userId") Long userId);

    // Получить топ‑N последних взаимодействий пользователя (по дате)
    List<Interaction> findTopNByUserIdOrderByTimestampDesc(@Param("userId") Long userId, Pageable pageable);

    // Получить оценку пользователя для конкретного события
    @Query("SELECT i.rating FROM Interaction i " +
            "WHERE i.userId = :userId AND i.eventId = :eventId")
    Float findRatingByUserIdAndEventId(@Param("userId") Long userId,
                                       @Param("eventId") Long eventId);

}
