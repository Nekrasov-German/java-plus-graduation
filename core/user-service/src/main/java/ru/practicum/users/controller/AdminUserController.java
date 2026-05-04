package ru.practicum.users.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.dto.NewUserRequest;
import ru.practicum.interaction.dto.UserDto;
import ru.practicum.interaction.dto.UserShortDto;
import ru.practicum.users.service.AdminUserService;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminUserController {
    private final AdminUserService service;

    @GetMapping("/short/{userId}")
    public UserShortDto getShortUser(@PathVariable(name = "userId") @Positive Long userId) {
        return service.getShortUser(userId);
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable(name = "userId") @Positive Long userId) {
        return service.getUser(userId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> findUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Positive Integer size
            ) {
        log.info("Admin: Получение пользователей: ids={}, from={},size={}", ids, from, size);
        return service.getUsers(ids, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto newUser(
            @RequestBody @Valid NewUserRequest dto
    ) {
        log.info("Admin: Добавление нового пользователя: {}", dto);
        return service.createUser(dto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @PathVariable(name = "userId") @Positive Long userId
    ) {
        log.info("Admin: Удаление пользователя с id={}", userId);
        service.deleteUser(userId);
    }
}
