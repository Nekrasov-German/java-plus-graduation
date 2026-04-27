package ru.practicum.client;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.dto.request.StatHitRequestDto;
import ru.practicum.dto.response.HitsCounterResponseDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StatClient {
    private static final Logger log = LoggerFactory.getLogger(StatClient.class);
    private final ClientStats clientStats;

    private static final LocalDateTime VERY_PAST = LocalDateTime.of(2000, 1, 1, 0, 0);

    public ResponseEntity<Void> hit(StatHitRequestDto dto) {
        try {
        return clientStats.saveHit(dto);
        } catch (FeignException e) {
            log.info("Ошибка сохранения статистики.");
            return null;
        }
    }

    public List<HitsCounterResponseDto> getHits(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            Boolean unique
    ) {

        try {
        return clientStats.getStats(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                uris, unique).getBody();
        } catch (FeignException e) {
            log.info("Ошибка получения статистики.");
            return List.of();
        }
    }

    public List<HitsCounterResponseDto> getHits(
            List<String> uris,
            Boolean unique
    ) {
        LocalDateTime start = VERY_PAST;
        LocalDateTime end = LocalDateTime.now();

        try {
            return clientStats.getStats(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    uris, unique).getBody();
        } catch (FeignException e) {
            log.info("Ошибка получения статистики.");
            return List.of();
        }
    }
}
