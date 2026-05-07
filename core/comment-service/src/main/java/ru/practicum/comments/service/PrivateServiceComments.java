package ru.practicum.comments.service;

import ru.practicum.interaction.dto.CommentDto;
import ru.practicum.interaction.dto.CommentRequestDto;

public interface PrivateServiceComments {
    CommentDto createComment(Long userId, Long eventId, CommentRequestDto comment);

    CommentDto updateComment(Long userId, Long eventId, Long commentId, CommentRequestDto comment);

    void deleteComment(Long userId, Long eventId, Long commentId);

    void addAndDeleteLikeComment(Long userId, Long eventId, Long commentId);
}
