package boogi.apiserver.domain.comment.repository;

import boogi.apiserver.domain.comment.domain.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;


public interface CommentRepositoryCustom {
    Slice<Comment> findParentCommentsWithMemberByPostId(Pageable pageable, Long postId);

    List<Comment> findChildCommentsWithMemberByParentCommentIds(List<Long> commentIds);

    Slice<Comment> getUserCommentPageByMemberIds(List<Long> memberIds, Pageable pageable);
}
