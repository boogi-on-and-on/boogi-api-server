package boogi.apiserver.domain.like.dao;

import boogi.apiserver.domain.like.domain.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    List<Like> findCommentLikesByCommentIdsAndMemberId(List<Long> commentId, Long memberId);

    Page<Like> findPostLikeWithMemberByPostId(Long postId, Pageable pageable);

    Page<Like> findCommentLikeWithMemberByCommentId(Long commentId, Pageable pageable);
}
