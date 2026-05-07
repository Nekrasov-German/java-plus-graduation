package ru.practicum.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.interaction.dto.NewUserRequest;
import ru.practicum.interaction.dto.UserDto;
import ru.practicum.interaction.dto.UserShortDto;
import ru.practicum.users.dal.UserRepository;
import ru.practicum.users.exceptions.ConflictException;
import ru.practicum.users.exceptions.NotFoundException;
import ru.practicum.users.mapper.UserMapper;
import ru.practicum.users.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserServiceImpl implements AdminUserService {
    private final UserRepository repository;

    @Override
    public UserShortDto getShortUser(Long userId) {
        return UserMapper.toUserShortDto(repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден")));
    }

    @Override
    public UserDto getUser(Long userId) {
        return UserMapper.toUserDto(repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден")));
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);

        Page<User> users;

        if (ids == null || ids.isEmpty()) {
            users = repository.findAll(pageable);
        } else {
            users = repository.findByIdIn(ids, pageable);
        }

        return users.getContent().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest dto) {
        if (repository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Адрес электронной почты уже существует.");
        }

        User user = UserMapper.toUserEntity(dto);
        User savedUser = repository.save(user);

        return UserMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден.");
        }
        repository.deleteById(id);
    }
}
