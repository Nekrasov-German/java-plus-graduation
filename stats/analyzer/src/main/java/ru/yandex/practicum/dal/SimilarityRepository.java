package ru.yandex.practicum.dal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.models.Similarity;

import java.util.List;
import java.util.Optional;

@Repository
public interface SimilarityRepository extends JpaRepository<Similarity, Long> {

    Optional<Similarity> findByEvent1AndEvent2(Long event1, Long event2);

    @Query("SELECT s FROM Similarity s " +
            "WHERE (s.event1 IN :eventIds OR s.event2 IN :eventIds) " +
            "ORDER BY s.similarity DESC")
    List<Similarity> findSimilarEventsForEvents(@Param("eventIds") List<Long> eventIds);

    // Получить K самых похожих событий для заданного (по similarity DESC)
    @Query("SELECT s FROM Similarity s " +
            "WHERE (s.event1 = :eventId OR s.event2 = :eventId) " +
            "AND s.event1 != s.event2 " +
            "ORDER BY s.similarity DESC")
    List<Similarity> findTopSimilarEvents(@Param("eventId") Long eventId,
                                          Pageable pageable);

    // Найти K ближайших соседей для предсказываемого мероприятия
    @Query("SELECT s FROM Similarity s " +
            "WHERE s.event1 = :targetEventId AND s.event2 IN :sourceEventIds " +
            "OR s.event2 = :targetEventId AND s.event1 IN :sourceEventIds " +
            "ORDER BY s.similarity DESC")
    List<Similarity> findKNearestNeighbors(@Param("targetEventId") Long targetEventId,
                                           @Param("sourceEventIds") List<Long> sourceEventIds,
                                           Pageable pageable);
}
