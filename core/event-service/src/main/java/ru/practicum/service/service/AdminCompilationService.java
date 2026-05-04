package ru.practicum.service.service;

import ru.practicum.interaction.dto.CompilationDto;
import ru.practicum.interaction.dto.NewCompilationDto;
import ru.practicum.interaction.dto.UpdateCompilationRequest;

public interface AdminCompilationService {
    CompilationDto createCompilation(NewCompilationDto dto);

    void deleteCompilation(Long id);

    CompilationDto updateCompilation(Long id, UpdateCompilationRequest request);
}
