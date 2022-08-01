package boogi.apiserver.domain.comment.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.exception.CommentMaxDepthOverException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class CommentValidationServiceTest {

    @InjectMocks
    CommentValidationService commentValidationService;

    @Mock
    CommentRepository commentRepository;

    @Nested
    @DisplayName("댓글 Depth 유효성 검사시")
    class MaxDepthOverValidationTest {

        @Test
        @DisplayName("댓글(Depth 0)일때 성공한다.")
        void DepthZero() {
            Comment result = commentValidationService.checkCommentMaxDepthOver(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("대댓글(Depth 1)일때 성공한다.")
        void DepthOne() {
            Comment parentComment = Comment.builder()
                    .id(1L)
                    .content("hello")
                    .parent(null)
                    .build();

            Comment childComment = Comment.builder()
                    .id(2L)
                    .content("hello2")
                    .parent(parentComment)
                    .build();
            given(commentRepository.findById(anyLong()))
                    .willReturn(Optional.of(parentComment));

            Comment result = commentValidationService.checkCommentMaxDepthOver(childComment.getParent().getId());

            assertThat(result).isEqualTo(parentComment);
        }

        @Test
        @DisplayName("대대댓글(Depth 2)일때 실패한다.")
        void DepthTwo() {
            Comment ppComment = Comment.builder()
                    .id(1L)
                    .content("hello")
                    .parent(null)
                    .build();

            Comment pComment = Comment.builder()
                    .id(2L)
                    .content("hello2")
                    .parent(ppComment)
                    .build();

            Comment comment = Comment.builder()
                    .id(3L)
                    .content("hello3")
                    .parent(pComment)
                    .build();
            given(commentRepository.findById(anyLong()))
                    .willReturn(Optional.of(pComment));

            assertThatThrownBy(() ->
                    commentValidationService.checkCommentMaxDepthOver(comment.getParent().getId()))
                    .isInstanceOf(CommentMaxDepthOverException.class);
        }
    }
}