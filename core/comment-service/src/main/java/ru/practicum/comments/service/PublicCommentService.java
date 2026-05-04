package ru.practicum.comments.service;

import ru.practicum.interaction.dto.CommentDto;

import java.util.List;

public interface PublicCommentService {
	List<CommentDto> getCommentByEventId(Long eventId, Integer from, Integer size);
}
