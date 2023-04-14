package boogi.apiserver.domain.comment.application;

import boogi.apiserver.domain.comment.repository.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.request.CreateCommentRequest;
import boogi.apiserver.domain.comment.exception.CanNotDeleteCommentException;
import boogi.apiserver.domain.comment.exception.CommentMaxDepthOverException;
import boogi.apiserver.domain.comment.exception.ParentCommentNotFoundException;
import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.repository.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class CommentCommandService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    private final LikeCommandService likeCommandService;

    private final MemberQueryService memberQueryService;

    public Long createComment(CreateCommentRequest request, Long userId) {
        Post findPost = postRepository.findPostById(request.getPostId());
        Member member = memberQueryService.getMember(userId, findPost.getCommunityId());

        Long parentCommentId = request.getParentCommentId();
        checkCommentMaxDepthOver(parentCommentId);

        Comment findParentComment = getParentComment(parentCommentId);
        Comment newComment = Comment.of(findPost, member, findParentComment, request.getContent());
        commentRepository.save(newComment);

        return newComment.getId();
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment findComment = commentRepository.findCommentById(commentId);

        validateCommentDeletable(userId, findComment);

        likeCommandService.removeAllCommentLikes(findComment.getId());
        findComment.deleteComment();
    }

    private void checkCommentMaxDepthOver(Long parentCommentId) {
        if (parentCommentId == null) {
            return;
        }
        Comment findParentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(ParentCommentNotFoundException::new);

        if (findParentComment.getParent() != null) {
            throw new CommentMaxDepthOverException();
        }
    }

    private Comment getParentComment(Long parentCommentId) {
        return parentCommentId == null ? null :
                commentRepository.findById(parentCommentId).orElse(null);
    }

    private void validateCommentDeletable(Long userId, Comment comment) {
        Long commentedUserId = comment.getMember().getUser().getId();

        if (commentedUserId.equals(userId)) {
            return;
        }

        Member sessionMember = memberQueryService.getMember(userId, comment.getPost().getCommunityId());
        if (!sessionMember.isOperator()) {
            throw new CanNotDeleteCommentException();
        }
    }
}
