package ru.practicum.service.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.dto.CompilationDto;
import ru.practicum.interaction.dto.EventShortDto;
import ru.practicum.service.dal.CompilationRepository;
import ru.practicum.service.error.NotFoundException;
import ru.practicum.service.mapper.CompilationMapper;
import ru.practicum.service.mapper.EventMapper;
import ru.practicum.service.model.Compilation;
import ru.practicum.service.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicCompilationServiceImpl implements PublicCompilationService {
    final CompilationRepository compilationRepository;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size, HttpServletRequest request) {
        log.info("PublicCompilationService: выгрузка подборок по заданным параметрам");
        List<Compilation> compilationsList = compilationRepository.findCompilations(pinned, from, size);
        log.info("{}", compilationsList);

        List<CompilationDto> result = new ArrayList<>();
        for (Compilation comp : compilationsList) {
            Set<EventShortDto> eventDtos = comp.getEvents().stream()
                    .map(event -> EventMapper.toEventShortDto(event, 0.0))
                    .collect(Collectors.toSet());

            CompilationDto compilationDto = CompilationMapper.toCompilationDto(comp, eventDtos);

            result.add(compilationDto);
        }

        return result;
    }

    @Override
    public CompilationDto getCompilationById(Long compId, HttpServletRequest request) {
        log.info("PublicCompilationService: поиск подборки с id: {}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Подборка с id: %d не найдена", compId)));

        Set<Event> events = compilation.getEvents();

        Set<EventShortDto> eventShortDtoList = events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toSet());

        return CompilationMapper.toCompilationDto(compilation, eventShortDtoList);
    }
}
