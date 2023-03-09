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
        Member joinedMember = memberQueryService.getMemberOfTheCommunity(userId, communityId);

        checkAlreadyPostLike(postId, joinedMember);

        Like newLike = Like.postOf(findPost, joinedMember);
        likeRepository.save(newLike);
        return newLike.getId();
    }

    public Long doCommentLike(Long commentId, Long userId) {
        Comment findComment = commentRepository.findByCommentId(commentId);
        Long communityId = findComment.getMember().getCommunity().getId();
        Member joinedMember = memberQueryService.getMemberOfTheCommunity(userId, communityId);

        checkAlreadyCommentLike(commentId, joinedMember);

        Like newLike = Like.commentOf(findComment, joinedMember);
        likeRepository.save(newLike);
        return newLike.getId();
    }

    public void doUnlike(Long likeId, Long userId) {
        Like findLike = likeRepository.findByLikeId(likeId);
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

    private void checkAlreadyPostLike(Long postId, Member joinedMember) {
        if (likeRepository.existsLikeByPostIdAndMemberId(postId, joinedMember.getId())) {
            throw new AlreadyDoPostLikeException();
        }
    }

    private void checkAlreadyCommentLike(Long commentId, Member joinedMember) {
        if (likeRepository.existsLikeByCommentIdAndMemberId(commentId, joinedMember.getId())) {
            throw new AlreadyDoCommentLikeException();
        }
    }
}
