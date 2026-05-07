package ru.practicum.service.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.interaction.dto.CompilationDto;
import ru.practicum.interaction.dto.EventShortDto;
import ru.practicum.interaction.dto.NewCompilationDto;
import ru.practicum.service.model.Compilation;

import java.util.*;

@UtilityClass
public class CompilationMapper {
    public Compilation newCompilationToEntity(NewCompilationDto dto) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned())
                .events(new HashSet<>())
                .build();
    }

    public CompilationDto toCompilationDto(Compilation compilation, Set<EventShortDto> eventsDto) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .events(eventsDto)
                .build();
    }
}
