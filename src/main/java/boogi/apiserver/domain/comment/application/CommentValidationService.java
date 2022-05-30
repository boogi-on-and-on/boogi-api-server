package boogi.apiserver.domain.comment.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.exception.CommentMaxDepthOverException;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import boogi.apiserver.global.error.exception.ErrorInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentValidationService {

    private final CommentRepository commentRepository;

    public Comment checkCommentMaxDepthOver(Long parentCommentId) {
        if (parentCommentId == null) {
            return null;
        }

        Comment findParentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> {
                    throw new EntityNotFoundException("해당 댓글의 부모 댓글을 찾을 수 없습니다", ErrorInfo.NOT_FOUND);
                });

        if (findParentComment.getParent() != null) {
            throw new CommentMaxDepthOverException();
        }
        return findParentComment;
    }
}
