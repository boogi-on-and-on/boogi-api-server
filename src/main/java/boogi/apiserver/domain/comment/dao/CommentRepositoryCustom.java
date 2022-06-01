package boogi.apiserver.domain.comment.dao;

import boogi.apiserver.domain.comment.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


public interface CommentRepositoryCustom {
    Page<Comment> getUserCommentPage(Pageable pageable, Long userId);

    Optional<Comment> findCommentWithMemberByCommentId(Long commentId);

    Page<Comment> findParentCommentsWithMemberByPostId(Pageable pageable, Long postId);

    List<Comment> findChildCommentsWithMemberByParentCommentIds(List<Long> commentIds);

    Optional<Comment> findCommentById(Long commentId);

    Page<Comment> getUserCommentPageByMemberIds(List<Long> memberIds, Pageable pageable);
}
