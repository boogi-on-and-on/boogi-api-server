package boogi.apiserver.domain.comment.dao;

import boogi.apiserver.domain.comment.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


public interface CommentRepositoryCustom {
    Page<Comment> getUserCommentPage(Pageable pageable, Long userId);

    Optional<Comment> findCommentWithMemberByCommentId(Long commentId);
}
