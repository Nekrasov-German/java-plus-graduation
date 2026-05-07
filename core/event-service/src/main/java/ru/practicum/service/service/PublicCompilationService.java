package ru.practicum.service.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.interaction.dto.CompilationDto;

import java.util.List;

public interface PublicCompilationService {
    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size, HttpServletRequest request);

    CompilationDto getCompilationById(Long compId, HttpServletRequest request);
}
