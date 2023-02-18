package boogi.apiserver.domain.comment.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.response.UserCommentPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentRepository commentRepository;

    public UserCommentPageResponse getUserComments(Pageable pageable, Long userId) {
        Slice<Comment> userCommentPage = commentRepository.getUserCommentPage(pageable, userId);

        return UserCommentPageResponse.of(userCommentPage);
    }
}
