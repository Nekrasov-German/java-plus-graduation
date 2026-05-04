package ru.practicum.users.service;

import ru.practicum.interaction.dto.NewUserRequest;
import ru.practicum.interaction.dto.UserDto;
import ru.practicum.interaction.dto.UserShortDto;

import java.util.List;

public interface AdminUserService {
    UserShortDto getShortUser(Long userId);

    UserDto getUser(Long userId);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    UserDto createUser(NewUserRequest dto);

    void deleteUser(Long id);

}
