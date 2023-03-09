package boogi.apiserver.domain.like.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtCommentResponse;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtPostResponse;
import boogi.apiserver.domain.like.exception.AlreadyDoLikeException;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeCommandService {

    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    private final PostQueryService postQueryService;

    private final LikeValidationService likeValidationService;

    public Like doLikeAtPost(Long postId, Long userId) {
        Post findPost = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 글이 존재하지 않습니다"));

        Long communityId = findPost.getCommunity().getId();
        Member joinedMember = memberRepository.findByUserIdAndCommunityId(userId, communityId)
                .orElseThrow(NotJoinedMemberException::new);

        if (likeValidationService.checkOnlyAlreadyDoPostLike(postId, joinedMember.getId())) {
            throw new AlreadyDoLikeException("이미 해당 글에 좋아요를 한 상태입니다");
        }

        Like newLike = Like.postOf(findPost, joinedMember);
        findPost.addLikeCount();
        likeRepository.save(newLike);
        return newLike;
    }

    public Like doLikeAtComment(Long commentId, Long userId) {
        Comment findComment = commentRepository.findCommentWithMemberByCommentId(commentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 댓글이 존재하지 않습니다"));

        Long communityId = findComment.getMember().getCommunity().getId();
        Member joinedMember = memberRepository.findByUserIdAndCommunityId(userId, communityId)
                .orElseThrow(NotJoinedMemberException::new);

        if (likeValidationService.checkOnlyAlreadyDoCommentLike(commentId, joinedMember.getId())) {
            throw new AlreadyDoLikeException("이미 해당 댓글에 좋아요를 한 상태입니다");
        }

        Like newLike = Like.commentOf(findComment, joinedMember);
        likeRepository.save(newLike);
        return newLike;
    }

    public void doUnlike(Long likeId, Long userId) {
        Like findLike = likeRepository.findLikeWithMemberById(likeId)
                .orElseThrow(EntityNotFoundException::new);

        Long DidLikedUser = findLike.getMember().getUser().getId();
        if (!DidLikedUser.equals(userId)) {
            throw new NotAuthorizedMemberException("요청한 유저가 좋아요를 한 유저와 다릅니다");
        }

        Post post = findLike.getPost();
        if (post != null) {
            post.removeLikeCount();
        }

        likeRepository.delete(findLike);
    }

    public void removePostLikes(Long postId) {
        likeRepository.deleteAllPostLikeByPostId(postId);
    }

    public void removeAllCommentLikes(Long commentId) {
        likeRepository.deleteAllCommentLikeByCommentId(commentId);
    }

    public LikeMembersAtPostResponse getLikeMembersAtPost(Long postId, Long userId, Pageable pageable) {
        Post findPost = postRepository.findByPostId(postId);

        Community postedCommunity = findPost.getCommunity();
        Member member = memberRepository.findByUserIdAndCommunityId(userId, postedCommunity.getId())
                .orElse(null);

        if (postedCommunity.isPrivate() && member == null) {
            throw new NotJoinedMemberException();
        }

        Slice<Like> likePage = likeRepository.findPostLikePageWithMemberByPostId(findPost.getId(), pageable);

        List<Long> userIds = likePage.getContent().stream()
                .map(like -> like.getMember().getUser().getId())
                .collect(Collectors.toList());

        List<User> users = userRepository.findUsersByIds(userIds);

        return LikeMembersAtPostResponse.of(users, likePage);
    }

    public LikeMembersAtCommentResponse getLikeMembersAtComment(Long commentId, Long userId, Pageable pageable) {
        Comment findComment = commentRepository.findById(commentId).orElseThrow(
                () -> new EntityNotFoundException("해당 댓글이 존재하지 않습니다"));

        Community commentedCommunity = findComment.getPost().getCommunity();
        Member member = memberRepository.findByUserIdAndCommunityId(userId, commentedCommunity.getId())
                .orElse(null);

        if (commentedCommunity.isPrivate() && member == null) {
            throw new NotJoinedMemberException();
        }

        Slice<Like> likePage = likeRepository.findCommentLikePageWithMemberByCommentId(findComment.getId(), pageable);

        List<Long> userIds = likePage.getContent().stream()
                .map(like -> like.getMember().getUser().getId())
                .collect(Collectors.toList());

        List<User> users = userRepository.findUsersByIds(userIds);

        return LikeMembersAtCommentResponse.of(users, likePage);
    }
}
