package ru.practicum.comments.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.interaction.dto.CommentAdminDto;

import java.util.List;

public interface AdminCommentService {
    List<CommentAdminDto> getAllComments(Long eventId, Long userId, String text, Pageable pageable);

    CommentAdminDto getCommentById(Long commentId);

    void deleteComment(Long commentId);
}
