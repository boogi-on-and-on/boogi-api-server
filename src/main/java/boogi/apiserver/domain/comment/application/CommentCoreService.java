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
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        return commentRepository.save(newComment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment findComment = commentRepository.findCommentWithMemberByCommentId(commentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 댓글이 존재하지 않습니다."));

        Long joinedCommunityId = findComment.getMember().getCommunity().getId();
        if (memberValidationService.hasAuth(userId, joinedCommunityId, findComment.getMember().getMemberType())) {
            likeCoreService.removeAllCommentLikes(findComment.getId());

            // TODO: 댓글 삭제시 대댓글도 삭제할지 여부 정하기

            Post post = findComment.getPost();
            if (post.getId() != null) {
                post.removeCommentCount();
            }
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
}
