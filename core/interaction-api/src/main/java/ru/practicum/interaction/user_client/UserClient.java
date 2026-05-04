package ru.practicum.interaction.user_client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.dto.NewUserRequest;
import ru.practicum.interaction.dto.UserDto;
import ru.practicum.interaction.dto.UserShortDto;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/admin/users/short/{userId}")
    UserShortDto getShortUser(@PathVariable(name = "userId") @Positive Long userId);

    @GetMapping("/admin/users/{userId}")
    UserDto getUser(@PathVariable(name = "userId") @Positive Long userId);

    @GetMapping("/admin/users")
    List<UserDto> findUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Positive Integer size
    );

    @PostMapping("/admin/users")
    UserDto newUser(
            @RequestBody @Valid NewUserRequest dto
    );

    @DeleteMapping("/admin/users/{userId}")
    void deleteUser(
            @PathVariable(name = "userId") @Positive Long userId
    );
}
