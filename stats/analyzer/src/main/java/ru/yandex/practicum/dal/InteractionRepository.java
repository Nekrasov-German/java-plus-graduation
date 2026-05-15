package ru.yandex.practicum.dal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.models.Interaction;

import java.util.List;
import java.util.Optional;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    Optional<Interaction> findByUserIdAndEventId(Long userId, Long eventId);

    @Query("SELECT CAST(SUM(i.rating) AS float) FROM Interaction i WHERE i.eventId = :eventId")
    Float getTotalRatingByEventId(@Param("eventId") Long eventId);

    // Получить все event_id, с которыми взаимодействовал пользователь
    @Query("SELECT DISTINCT i.eventId FROM Interaction i WHERE i.userId = :userId")
    List<Long> findEventIdsByUserId(@Param("userId") Long userId);

    // Получить топ‑N последних взаимодействий пользователя (по дате)
    List<Interaction> findTopNByUserIdOrderByTimestampDesc(@Param("userId") Long userId, Pageable pageable);

    // Проверить, взаимодействовал ли пользователь с событием
    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    // Получить оценку пользователя для конкретного события
    @Query("SELECT i.rating FROM Interaction i " +
            "WHERE i.userId = :userId AND i.eventId = :eventId")
    Float findRatingByUserIdAndEventId(@Param("userId") Long userId,
                                       @Param("eventId") Long eventId);

}
