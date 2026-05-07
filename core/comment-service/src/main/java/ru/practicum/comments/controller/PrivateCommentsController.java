package ru.practicum.comments.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.service.PrivateServiceComments;
import ru.practicum.interaction.dto.CommentDto;
import ru.practicum.interaction.dto.CommentRequestDto;

@Slf4j
@Controller
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/events/{eventId}/comments")
public class PrivateCommentsController {
    private final PrivateServiceComments privateServiceComments;

    @PostMapping
    public ResponseEntity<CommentDto> createComment(@PathVariable(value = "userId") Long userId,
                                                    @PathVariable(value = "eventId") Long eventId,
                                                    @RequestBody @Valid CommentRequestDto commentRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(privateServiceComments.createComment(userId, eventId, commentRequestDto));
    }

    @PostMapping("/{commentId}/likes")
    public ResponseEntity<Void> addAndDeleteLikeComment(@PathVariable(value = "userId") Long userId,
                                               @PathVariable(value = "eventId") Long eventId,
                                               @PathVariable(value = "commentId") Long commentId) {
        privateServiceComments.addAndDeleteLikeComment(userId, eventId, commentId);
        return ResponseEntity.ok().body(null);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable(value = "userId") Long userId,
                                              @PathVariable(value = "eventId") Long eventId,
                                              @PathVariable(value = "commentId") Long commentId,
                                              @RequestBody @Valid CommentRequestDto commentRequestDto) {
        return ResponseEntity.ok()
                .body(privateServiceComments.updateComment(userId, eventId, commentId, commentRequestDto));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable(value = "userId") Long userId,
                                              @PathVariable(value = "eventId") Long eventId,
                                              @PathVariable(value = "commentId") Long commentId) {
        privateServiceComments.deleteComment(userId, eventId, commentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
