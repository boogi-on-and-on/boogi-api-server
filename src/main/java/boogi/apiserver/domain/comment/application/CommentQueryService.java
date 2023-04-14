package boogi.apiserver.domain.comment.application;

import boogi.apiserver.domain.comment.repository.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.response.CommentsAtPostResponse;
import boogi.apiserver.domain.comment.dto.response.UserCommentPageResponse;
import boogi.apiserver.domain.like.repository.LikeRepository;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.repository.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.repository.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    private final MemberQueryService memberQueryService;

    public CommentsAtPostResponse getCommentsAtPost(Long postId, Long userId, Pageable pageable) {
        Post findPost = postRepository.findPostById(postId);

        Member sessionMember = memberQueryService.getViewableMember(userId, findPost.getCommunity());
        Slice<Comment> commentPage = commentRepository.findParentCommentsWithMemberByPostId(pageable, postId);

        List<Long> parentCommentIds = toCommentIdList(commentPage.getContent());

        List<Comment> childComments = commentRepository.findChildCommentsWithMemberByParentCommentIds(parentCommentIds);

        List<Long> commentIds = getAllCommentIds(parentCommentIds, childComments);
        Map<Long, Long> findCommentLikeCountMap = likeRepository.getCommentLikeCountsByCommentIds(commentIds);

        Long sessionMemberId = sessionMember.getId();
        Map<Long, Like> sessionMemberCommentLikeMap = getSessionMemberCommentLikeMap(commentIds, sessionMemberId);

        return CommentsAtPostResponse.of(
                sessionMemberCommentLikeMap,
                findCommentLikeCountMap,
                childComments,
                commentPage,
                sessionMemberId
        );
    }

    public UserCommentPageResponse getUserComments(Long userId, Long sessionUserId, Pageable pageable) {
        List<Long> findMemberIds = getMemberIdsForQueryUserPost(userId, sessionUserId);

        Slice<Comment> slice = commentRepository.getUserCommentPageByMemberIds(findMemberIds, pageable);

        return UserCommentPageResponse.of(slice);
    }

    private List<Long> getMemberIdsForQueryUserPost(Long userId, Long sessionUserId) {
        if (userId.equals(sessionUserId)) {
            return memberRepository.findMemberIdsForQueryUserPost(sessionUserId);
        } else {
            userRepository.findUserById(userId);
            return memberRepository.findMemberIdsForQueryUserPost(userId, sessionUserId);
        }
    }

    private List<Long> getAllCommentIds(List<Long> parentCommentIds, List<Comment> childComments) {
        List<Long> commentIds = toCommentIdList(childComments);
        commentIds.addAll(parentCommentIds);
        return commentIds;
    }

    private Map<Long, Like> getSessionMemberCommentLikeMap(List<Long> commentIds, Long sessionMemberId) {
        if (sessionMemberId == null) {
            return new HashMap<>();
        }
        List<Like> commentLikes = likeRepository.findCommentLikesByCommentIdsAndMemberId(commentIds, sessionMemberId);
        return commentLikes.stream().collect(Collectors.toMap(c -> c.getComment().getId(), c -> c));
    }

    private List<Long> toCommentIdList(List<Comment> comments) {
        return comments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
    }
}
