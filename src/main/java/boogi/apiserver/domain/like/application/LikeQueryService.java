package boogi.apiserver.domain.like.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtCommentResponse;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtPostResponse;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LikeQueryService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    private final MemberQueryService memberQueryService;

    public Long getPostLikeId(Long postId, Long memberId) {
        return likeRepository.findPostLikeByPostIdAndMemberId(postId, memberId)
                .map(Like::getId)
                .orElse(null);
    }

    public LikeMembersAtPostResponse getLikeMembersAtPost(Long postId, Long userId, Pageable pageable) {
        Post findPost = postRepository.findByPostId(postId);
        memberQueryService.getViewableMember(userId, findPost.getCommunity());

        Slice<Like> likePage = likeRepository.findPostLikePageWithMemberByPostId(findPost.getId(), pageable);
        List<User> users = findLikedUser(likePage);

        return LikeMembersAtPostResponse.of(users, likePage);
    }

    public LikeMembersAtCommentResponse getLikeMembersAtComment(Long commentId, Long userId, Pageable pageable) {
        Comment findComment = commentRepository.findCommentById(commentId);
        memberQueryService.getViewableMember(userId, findComment.getPost().getCommunity());

        Slice<Like> likePage = likeRepository.findCommentLikePageWithMemberByCommentId(findComment.getId(), pageable);
        List<User> users = findLikedUser(likePage);

        return LikeMembersAtCommentResponse.of(users, likePage);
    }

    private List<User> findLikedUser(Slice<Like> likePage) {
        List<Long> userIds = likePage.getContent().stream()
                .map(like -> like.getMember().getUser().getId())
                .collect(Collectors.toList());

        return userRepository.findUsersByIds(userIds);
    }
}
