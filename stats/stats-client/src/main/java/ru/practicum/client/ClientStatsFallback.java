package ru.practicum.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.dto.request.StatHitRequestDto;
import ru.practicum.dto.response.HitsCounterResponseDto;

import java.util.List;

@Component
public class ClientStatsFallback implements ClientStats {
    @Override
    public ResponseEntity<Void> saveHit(StatHitRequestDto dto) {
        return ResponseEntity.status(503).build(); // Service Unavailable
    }

    @Override
    public ResponseEntity<List<HitsCounterResponseDto>> getStats(String start, String end, List<String> uris, boolean unique) {
        return ResponseEntity.ok(List.of());
    }
}
