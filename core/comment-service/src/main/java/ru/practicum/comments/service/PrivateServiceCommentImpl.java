package ru.practicum.comments.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.comments.dal.CommentLikeRepository;
import ru.practicum.comments.dal.CommentRepository;
import ru.practicum.comments.exceptions.ConflictException;
import ru.practicum.comments.exceptions.NotFoundException;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.model.CommentLike;
import ru.practicum.interaction.dto.CommentDto;
import ru.practicum.interaction.dto.CommentRequestDto;
import ru.practicum.interaction.dto.EventFullDto;
import ru.practicum.interaction.dto.UserShortDto;
import ru.practicum.interaction.dto.enums.State;
import ru.practicum.interaction.event_client.EventClient;
import ru.practicum.interaction.user_client.UserClient;

@Service
@RequiredArgsConstructor
public class PrivateServiceCommentImpl implements PrivateServiceComments {
    private final EventClient eventClient;
    private final UserClient userClient;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Override
    public CommentDto createComment(Long userId, Long eventId, CommentRequestDto commentRequestDto) {

        UserShortDto user;
        try {
            user = userClient.getShortUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Пользователь не найден");
        }

        EventFullDto event;
        try {
            event = eventClient.getInfoEvent(userId, eventId).getBody();
        } catch (FeignException e) {
            throw new NotFoundException("Такого события или пользователя не найдено.");
        }

        if (event.getState().equals(State.PENDING)) {
            throw new NotFoundException("Такого события не найдено.");
        }
        if (event.getInitiatorDto().getId().equals(userId)) {
            throw new ConflictException("Нельзя комментировать свое событие.");
        }

        return CommentMapper
                .commentToCommentDto(
                        commentRepository.save(CommentMapper.commentDtoToComment(commentRequestDto, user, event)),user);
    }

    @Override
    public CommentDto updateComment(Long userId, Long eventId, Long commentId, CommentRequestDto commentRequestDto) {

        UserShortDto user;
        try {
            user = userClient.getShortUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Пользователь не найден");
        }

        EventFullDto event;
        try {
            event = eventClient.getInfoEvent(userId, eventId).getBody();
        } catch (FeignException e) {
            throw new NotFoundException("Такого события или пользователя не найдено.");
        }

        if (event.getInitiatorDto().getId().equals(userId)) {
            throw new ConflictException("Нельзя комментировать свое событие.");
        }
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден."));

        comment.setText(commentRequestDto.getText());

        return CommentMapper.commentToCommentDto(commentRepository.save(comment), user);
    }

    @Override
    public void deleteComment(Long userId, Long eventId, Long commentId) {

        try {
            userClient.getShortUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Пользователь не найден");
        }

        try {
            eventClient.getInfoEvent(userId, eventId).getBody();
        } catch (FeignException e) {
            throw new NotFoundException("Такого события или пользователя не найдено.");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден."));
        if (comment.getAuthor().equals(userId)) {
            commentRepository.delete(comment);
        } else {
            throw new ConflictException("Невозможно удалить чужой комментарий.");
        }
    }

    @Override
    public void addAndDeleteLikeComment(Long userId, Long eventId, Long commentId) {

        try {
            userClient.getShortUser(userId);
        } catch (FeignException e) {
            throw new NotFoundException("Пользователь не найден");
        }

        try {
            eventClient.getInfoEvent(userId, eventId).getBody();
        } catch (FeignException e) {
            throw new NotFoundException("Такого события или пользователя не найдено.");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден."));
        if (userId.equals(comment.getAuthor())) {
            throw new ConflictException("Невозможно поставить лайк на свой комментарий.");
        }

        CommentLike like = CommentLike.builder()
                .userId(userId)
                .commentId(commentId)
                .build();
        boolean exists = commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
        if (!exists) {
            commentLikeRepository.save(like);
        } else {
            commentLikeRepository.delete(like);
        }
    }
}
