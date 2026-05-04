package ru.practicum.comments.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.comments.dal.CommentLikeRepository;
import ru.practicum.comments.dal.CommentRepository;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.model.CommentLike;
import ru.practicum.interaction.dto.CommentDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class PublicCommentServiceImpl implements PublicCommentService {
	final CommentRepository commentRepository;
	final CommentLikeRepository commentLikeRepository;

	@Override
	public List<CommentDto> getCommentByEventId(Long eventId, Integer from, Integer size) {
		log.info("PublicCommentServiceImpl: Поиск комментов с заданными параметрами");
		Pageable pageable = PageRequest.of(from / size, size);
		List<Comment> commentList = commentRepository.findByEventOrderByCreatedOnDesc(eventId, pageable);
		log.info("PublicCommentServiceImpl: {}", commentList);

		log.info("PublicCommentServiceImpl: Поиск лайков комментов");
		List<Long> commentsIds = commentList.stream().map(Comment::getId).toList();
		List<CommentLike> commentLikeList = commentLikeRepository.findByCommentIdIn(commentsIds);

		Map<Long, Integer> commentLikesMap = commentLikeList.stream()
				.collect(Collectors.groupingBy(CommentLike::getCommentId, Collectors.summingInt(like -> 1)));
		log.info("PublicCommentServiceImpl: {}", commentLikesMap);

		return commentList.stream()
				.map(comment ->
						CommentMapper.toCommentDtoWithLikes(comment, commentLikesMap.getOrDefault(comment.getId(), 0)))
				.toList();
	}
}
