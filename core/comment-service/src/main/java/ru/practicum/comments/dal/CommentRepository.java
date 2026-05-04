package ru.practicum.comments.dal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.comments.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByAuthorOrderByCreatedOnDesc(Long authorId, Pageable pageable);

    List<Comment> findAllByEventAndAuthorOrderByCreatedOnDesc(Long eventId, Long authorId, Pageable pageable);

    List<Comment> findByEventOrderByCreatedOnDesc(Long eventId, Pageable pageable);

    List<Comment> findByTextContainingIgnoreCaseOrderByCreatedOnDesc(String text, Pageable pageable);
}
