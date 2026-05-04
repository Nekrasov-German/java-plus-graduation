package ru.practicum.interaction.comment_client;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.interaction.dto.CommentAdminDto;

import java.util.List;

@FeignClient(name = "comment-service")
public interface CommentClient {

    @GetMapping("/admin/comments")
    List<CommentAdminDto> findAll(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    );

    @GetMapping("/admin/comments/{commentId}")
    CommentAdminDto findOne(
            @PathVariable(name = "commentId") Long commentId
    );

    @DeleteMapping("/admin/comments/{commentId}")
    void deleteComment(
            @PathVariable(name = "commentId") @Positive Long commentId);
}
