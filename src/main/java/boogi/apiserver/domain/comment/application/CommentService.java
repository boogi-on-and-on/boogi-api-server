package boogi.apiserver.domain.comment.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.request.CreateComment;
import boogi.apiserver.domain.comment.dto.response.CommentsAtPost;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.comment.dto.response.UserCommentPage;
import boogi.apiserver.domain.like.application.LikeService;
import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import boogi.apiserver.global.webclient.push.MentionType;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final PostQueryService postQueryService;

    private final UserRepository userRepository;

    private final MemberRepository memberRepository;
    private final MemberValidationService memberValidationService;

    private final LikeRepository likeRepository;
    private final LikeService likeService;

    private final CommentRepository commentRepository;
    private final CommentValidationService commentValidationService;

    private final SendPushNotification sendPushNotification;
    private final PostRepository postRepository;

    @Transactional
    public Comment createComment(CreateComment createComment, Long userId) {
        Post findPost = postRepository.findByPostId(createComment.getPostId());

        Member member = memberRepository.findByUserIdAndCommunityId(userId, findPost.getCommunity().getId())
                .orElseThrow(NotJoinedMemberException::new);

        Long parentCommentId = createComment.getParentCommentId();
        commentValidationService
                .checkCommentMaxDepthOver(parentCommentId);

        Comment findParentComment = parentCommentId == null ? null : commentRepository.findById(parentCommentId)
                .orElse(null);

        Comment newComment = Comment.of(findPost, member, findParentComment, createComment.getContent());
        findPost.addCommentCount();

        commentRepository.save(newComment);
        Long savedCommentId = newComment.getId();

        sendPushNotification.commentNotification(savedCommentId);
        if (createComment.getMentionedUserIds().isEmpty() == false) {
            sendPushNotification.mentionNotification(createComment.getMentionedUserIds(), savedCommentId, MentionType.COMMENT);
        }

        return newComment;
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment findComment = commentRepository.findCommentWithMemberByCommentId(commentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 댓글이 존재하지 않습니다."));

        Long joinedCommunityId = findComment.getMember().getCommunity().getId();
        Long commentedUserId = findComment.getMember().getUser().getId();
        if (commentedUserId.equals(userId) ||
                memberValidationService.hasAuth(userId, joinedCommunityId, MemberType.SUB_MANAGER)) {
            likeService.removeAllCommentLikes(findComment.getId());

            findComment.deleteComment();
        } else {
            throw new NotAuthorizedMemberException("해당 댓글의 삭제 권한이 없습니다.");
        }
    }

    public CommentsAtPost getCommentsAtPost(Long postId, Long userId, Pageable pageable) {
        Post findPost = postRepository.findByPostId(postId);

        Community postedCommunity = findPost.getCommunity();
        Member member = memberRepository.findByUserIdAndCommunityId(userId, postedCommunity.getId())
                .orElse(null);

        if (postedCommunity.isPrivate() && member == null) {
            throw new NotJoinedMemberException();
        }

        Slice<Comment> commentPage = commentRepository.findParentCommentsWithMemberByPostId(pageable, postId);

        List<Comment> parentComments = commentPage.getContent().stream()
                .map(c -> {
                    if (c.getDeletedAt() != null) {
                        return Comment.deletedOf(c.getId(), c.getDeletedAt());
                    }
                    return c;
                })
                .collect(Collectors.toList());

        List<Long> parentCommentIds = parentComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        List<Comment> childComments = commentRepository.findChildCommentsWithMemberByParentCommentIds(parentCommentIds);

        List<Long> commentIds = childComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        commentIds.addAll(parentCommentIds);

        Map<Long, Long> findCommentCountMap = (commentIds.isEmpty()) ? new HashMap<>() :
                likeRepository.getCommentLikeCountsByCommentIds(commentIds);

        Long joinedMemberId = (member == null) ? null : member.getId();
        Map<Long, Like> commentLikes = (joinedMemberId == null) ? null :
                likeRepository.findCommentLikesByCommentIdsAndMemberId(commentIds, joinedMemberId).stream()
                        .collect(Collectors.toMap(c -> c.getComment().getId(), c -> c));

        Map<Long, List<CommentsAtPost.ChildCommentInfo>> childCommentInfos = childComments.stream()
                .map(c -> createChildCommentInfo(
                        joinedMemberId,
                        commentLikes,
                        c,
                        findCommentCountMap.getOrDefault(c.getId(), 0L)))
                .collect(Collectors.groupingBy(CommentsAtPost.ChildCommentInfo::getParentId, HashMap::new, Collectors.toCollection(ArrayList::new)));

        List<CommentsAtPost.ParentCommentInfo> commentInfos = parentComments.stream()
                .filter(c -> (c.getDeletedAt() == null || childCommentInfos.get(c.getId()) != null))
                .map(c -> createParentCommentInfo(joinedMemberId,
                        commentLikes,
                        childCommentInfos,
                        c,
                        findCommentCountMap.getOrDefault(c.getId(), 0L)))
                .collect(Collectors.toList());

        return CommentsAtPost.of(commentInfos, commentPage);
    }

    private CommentsAtPost.ChildCommentInfo createChildCommentInfo(Long joinedMemberId, Map<Long, Like> commentLikes, Comment c, Long likeCount) {
        Like like = (commentLikes == null) ? null : commentLikes.get(c.getId());
        Long likeId = (like == null) ? null : like.getId();
        Member commentedMember = c.getMember();
        Long commentedMemberId = (commentedMember == null) ? null : commentedMember.getId();
        Boolean me = (commentedMember != null && commentedMemberId.equals(joinedMemberId))
                ? Boolean.TRUE : Boolean.FALSE;
        Long parentId = c.getParent().getId();
        return CommentsAtPost.ChildCommentInfo.toDto(c, likeId, me, parentId, likeCount);
    }

    private CommentsAtPost.ParentCommentInfo createParentCommentInfo(Long joinedMemberId, Map<Long, Like> commentLikes, Map<Long, List<CommentsAtPost.ChildCommentInfo>> childCommentInfos, Comment c, Long likeCount) {
        Like like = (commentLikes == null) ? null : commentLikes.get(c.getId());
        Long likeId = (like == null) ? null : like.getId();
        Member commentedMember = c.getMember();
        Long commentedMemberId = (commentedMember == null) ? null : commentedMember.getId();
        Boolean me = (commentedMember != null && commentedMemberId.equals(joinedMemberId))
                ? Boolean.TRUE : Boolean.FALSE;
        return CommentsAtPost.ParentCommentInfo.toDto(c, likeId, me, childCommentInfos.get(c.getId()), likeCount);
    }

    public UserCommentPage getUserComments(Long userId, Long sessionUserId, Pageable pageable) {
        List<Long> findMemberIds;

        if (userId.equals(sessionUserId)) {
            findMemberIds = memberRepository
                    .findMemberIdsForQueryUserPost(sessionUserId);
        } else {
            userRepository.findUserById(userId).orElseThrow(() ->
                    new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

            findMemberIds = memberRepository
                    .findMemberIdsForQueryUserPost(userId, sessionUserId);
        }

        Slice<Comment> slice = commentRepository.getUserCommentPageByMemberIds(findMemberIds, pageable);

        return UserCommentPage.of(slice);
    }
}
