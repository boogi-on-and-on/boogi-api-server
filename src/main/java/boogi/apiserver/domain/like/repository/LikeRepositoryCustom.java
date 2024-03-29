package boogi.apiserver.domain.like.repository;

import boogi.apiserver.domain.like.domain.Like;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Map;

public interface LikeRepositoryCustom {

    void deleteAllPostLikeByPostId(Long postId);

    void deleteAllCommentLikeByCommentId(Long commentId);

    boolean existsLikeByPostIdAndMemberId(Long postId, Long memberId);

    boolean existsLikeByCommentIdAndMemberId(Long commentId, Long memberId);

    List<Like> findCommentLikesByCommentIdsAndMemberId(List<Long> commentId, Long memberId);

    Slice<Like> findPostLikePageWithMemberByPostId(Long postId, Pageable pageable);

    Slice<Like> findCommentLikePageWithMemberByCommentId(Long commentId, Pageable pageable);

    Map<Long, Long> getCommentLikeCountsByCommentIds(List<Long> commentIds);
}
