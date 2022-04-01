package boogi.apiserver.domain.comment.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.CreateComment;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentCoreService {

    private final PostQueryService postQueryService;

    private final MemberValidationService memberValidationService;

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
                (findParentComment == null) ? Boolean.FALSE : Boolean.TRUE
        );
        return commentRepository.save(newComment);
    }
}
