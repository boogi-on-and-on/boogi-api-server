package boogi.apiserver.domain.like.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.exception.AlreadyDoCommentLikeException;
import boogi.apiserver.domain.like.exception.AlreadyDoPostLikeException;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeCommandService {
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    private final MemberQueryService memberQueryService;

    public Long doPostLike(Long postId, Long userId) {
        Post findPost = postRepository.findByPostId(postId);
        Long communityId = findPost.getCommunity().getId();
        Member joinedMember = memberQueryService.getMember(userId, communityId);

        validateAlreadyPostLike(postId, joinedMember);

        Like newLike = Like.ofPost(findPost, joinedMember);
        likeRepository.save(newLike);
        return newLike.getId();
    }

    public Long doCommentLike(Long commentId, Long userId) {
        Comment findComment = commentRepository.findCommentById(commentId);
        Long communityId = findComment.getPost().getCommunityId();
        Member joinedMember = memberQueryService.getMember(userId, communityId);

        validateAlreadyCommentLike(commentId, joinedMember);

        Like newLike = Like.ofComment(findComment, joinedMember);
        likeRepository.save(newLike);
        return newLike.getId();
    }

    public void doUnlike(Long likeId, Long userId) {
        Like findLike = likeRepository.findLikeById(likeId);
        findLike.validateLikedUser(userId);

        findLike.removeLikeCount();
        likeRepository.delete(findLike);
    }

    public void removePostLikes(Long postId) {
        likeRepository.deleteAllPostLikeByPostId(postId);
    }

    public void removeAllCommentLikes(Long commentId) {
        likeRepository.deleteAllCommentLikeByCommentId(commentId);
    }

    private void validateAlreadyPostLike(Long postId, Member member) {
        if (likeRepository.existsLikeByPostIdAndMemberId(postId, member.getId())) {
            throw new AlreadyDoPostLikeException();
        }
    }

    private void validateAlreadyCommentLike(Long commentId, Member member) {
        if (likeRepository.existsLikeByCommentIdAndMemberId(commentId, member.getId())) {
            throw new AlreadyDoCommentLikeException();
        }
    }
}
