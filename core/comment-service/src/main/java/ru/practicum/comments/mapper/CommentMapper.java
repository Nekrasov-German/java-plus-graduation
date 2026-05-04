package ru.practicum.comments.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.comments.model.Comment;
import ru.practicum.interaction.dto.*;

@UtilityClass
public class CommentMapper {
    public Comment commentDtoToComment(CommentRequestDto commentRequestDto, UserShortDto user, EventFullDto event) {
        return Comment.builder()
                .text(commentRequestDto.getText())
                .author(user.getId())
                .event(event.getId())
                .build();
    }

    public CommentDto commentToCommentDto(Comment comment, UserShortDto user) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(user.getName())
                .eventId(comment.getEvent())
                .create(comment.getCreatedOn())
                .build();
    }

    public CommentAdminDto commentToCommentAdminDto(Comment comment) {
        return CommentAdminDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthor())
                .eventId(comment.getEvent())
                .createdOn(comment.getCreatedOn())
                .likesCount(comment.getLikesCount())
                .build();
    }

    public CommentDto toCommentDtoWithLikes(Comment comment, Integer likes) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .eventId(comment.getEvent())
                .create(comment.getCreatedOn())
                .like(likes)
                .build();
    }
}
