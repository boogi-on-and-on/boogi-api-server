package boogi.apiserver.domain.like.dao;

import boogi.apiserver.domain.like.domain.Like;

import java.util.List;
import java.util.Optional;

public interface LikeRepositoryCustom {

    List<Like> findPostLikesByPostId(Long postId);

    void deleteAllPostLikeByPostId(Long postId);

    void deleteAllCommentLikeByCommentId(Long commentId);

    boolean existsLikeByPostIdAndMemberId(Long postId, Long memberId);

    boolean existsLikeByCommentIdAndMemberId(Long commentId, Long memberId);

    Optional<Like> findPostLikeWithMemberByLikeId(Long likeId);

    Optional<Like> findPostLikeByPostIdAndMemberId(Long postId, Long memberId);

    Optional<Like> findCommentLikeByCommentIdAndMemberId(Long commentId, Long memberId);
}
