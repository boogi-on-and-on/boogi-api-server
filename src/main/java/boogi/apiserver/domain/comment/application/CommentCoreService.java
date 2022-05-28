package boogi.apiserver.domain.comment.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.CreateComment;
import boogi.apiserver.domain.like.dto.LikeMembersAtComment;
import boogi.apiserver.domain.community.community.application.CommunityValidationService;
import boogi.apiserver.domain.like.application.LikeCoreService;
import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.comment.dto.CommentsAtPost;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import boogi.apiserver.global.webclient.push.MentionType;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class CommentCoreService {

    private final PostQueryService postQueryService;

    private final MemberRepository memberRepository;
    private final MemberValidationService memberValidationService;

    private final CommunityValidationService communityValidationService;

    private final LikeRepository likeRepository;
    private final LikeCoreService likeCoreService;

    private final CommentRepository commentRepository;
    private final CommentValidationService commentValidationService;

    private final SendPushNotification sendPushNotification;

    @Transactional
    public Comment createComment(CreateComment createComment, Long userId) {
        Post findPost = postQueryService.getPost(createComment.getPostId());

        Member member = memberValidationService.checkMemberJoinedCommunity(userId, findPost.getCommunity().getId());

        Comment findParentComment = commentValidationService
                .checkCommentMaxDepthOver(createComment.getParentCommentId());

        Comment newComment = Comment.of(findPost, member, findParentComment, createComment.getContent());
        newComment.setChild(
                (findParentComment == null) ? Boolean.FALSE : Boolean.TRUE);
        findPost.addCommentCount();

        Comment savedComment = commentRepository.save(newComment);
        Long savedCommentId = savedComment.getId();

        sendPushNotification.commentNotification(savedCommentId);
        if (createComment.getMentionedUserIds().isEmpty() == false) {
            sendPushNotification.mentionNotification(createComment.getMentionedUserIds(), savedCommentId, MentionType.COMMENT);
        }

        return savedComment;
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment findComment = commentRepository.findCommentWithMemberByCommentId(commentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 댓글이 존재하지 않습니다."));

        Long joinedCommunityId = findComment.getMember().getCommunity().getId();
        if (memberValidationService.hasAuth(userId, joinedCommunityId, MemberType.SUB_MANAGER)) {
            likeCoreService.removeAllCommentLikes(findComment.getId());

            findComment.deleteComment();
        }
    }

    public LikeMembersAtComment getLikeMembersAtComment(Long commentId, Long userId, Pageable pageable) {
        Comment findComment = commentRepository.findById(commentId).orElseThrow(
                () -> new EntityNotFoundException("해당 댓글이 존재하지 않습니다"));

        Long commentedCommunityId = findComment.getPost().getCommunity().getId();
        List<Member> findMemberResult = memberRepository.findByUserIdAndCommunityId(userId, commentedCommunityId);
        Member member = (findMemberResult.isEmpty()) ? null : findMemberResult.get(0);

        if (communityValidationService.checkOnlyPrivateCommunity(commentedCommunityId) && member == null) {
            throw new NotJoinedMemberException();
        }

        Page<Like> likePage = likeRepository.findCommentLikeWithMemberByCommentId(findComment.getId(), pageable);

        List<User> users = likePage.getContent().stream()
                .map(like -> like.getMember().getUser())
                .collect(Collectors.toList());

        return new LikeMembersAtComment(users, likePage);
    }


    public CommentsAtPost getCommentsAtPost(Long postId, Long userId, Pageable pageable) {
        Post findPost = postQueryService.getPost(postId);

        Long postedCommunityId = findPost.getCommunity().getId();
        List<Member> findMemberResult = memberRepository.findByUserIdAndCommunityId(userId, postedCommunityId);
        Member member = (findMemberResult.isEmpty()) ? null : findMemberResult.get(0);

        if (communityValidationService.checkOnlyPrivateCommunity(postedCommunityId) && member == null) {
            throw new NotJoinedMemberException();
        }

        Page<Comment> commentPage = commentRepository.findParentCommentsWithMemberByPostId(pageable, postId);

        List<Comment> parentComments = commentPage.getContent().stream()
                .map(c -> (c.getDeletedAt() == null && c.getCanceledAt() == null) ?
                        c : Comment.deletedOf(c.getId()))
                .collect(Collectors.toList());

        List<Long> parentCommentIds = parentComments.stream()
                .map(c -> c.getId())
                .collect(Collectors.toList());

        List<Comment> childComments = commentRepository.findChildCommentsWithMemberByParentCommentIds(parentCommentIds);

        List<Long> commentIds = childComments.stream()
                .map(c -> c.getId())
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
                .collect(Collectors.groupingBy(c -> c.getParentId(), HashMap::new, Collectors.toCollection(ArrayList::new)));

        List<CommentsAtPost.ParentCommentInfo> commentInfos = parentComments.stream()
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
}
