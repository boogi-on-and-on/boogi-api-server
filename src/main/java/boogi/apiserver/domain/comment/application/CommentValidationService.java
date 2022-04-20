package boogi.apiserver.domain.comment.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.exception.CommentMaxDepthOverException;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
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
                .orElseThrow(EntityNotFoundException::new);

        if (findParentComment.getParent() != null) {
            throw new CommentMaxDepthOverException();
        }
        return findParentComment;
    }
}
