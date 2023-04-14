package boogi.apiserver.domain.comment.repository;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.exception.CommentNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
    List<Comment> findByPostId(Long postId);

    default Comment findCommentById(Long commentId) {
        return this.findById(commentId).orElseThrow(CommentNotFoundException::new);
    }
}