package boogi.apiserver.domain.comment.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.exception.CommentMaxDepthOverException;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
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
            commentValidationService.checkCommentMaxDepthOver(null);

            verify(commentRepository, times(0)).findById(anyLong());
        }

        @Test
        @DisplayName("대댓글(Depth 1)일때 성공한다.")
        void DepthOne() {
            final Comment parentComment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(parentComment, "id", 1L);
            ReflectionTestUtils.setField(parentComment, "content", "hello");

            final Comment childComment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(childComment, "id", 2L);
            ReflectionTestUtils.setField(childComment, "content", "hello2");
            ReflectionTestUtils.setField(childComment, "parent", parentComment);

            given(commentRepository.findById(anyLong()))
                    .willReturn(Optional.of(parentComment));

            commentValidationService.checkCommentMaxDepthOver(childComment.getParent().getId());

            verify(commentRepository, times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("대대댓글(Depth 2)일때 실패한다.")
        void DepthTwo() {

            final Comment ppComment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(ppComment, "id", 1L);
            ReflectionTestUtils.setField(ppComment, "content", "hello");

            final Comment pComment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(pComment, "id", 2L);
            ReflectionTestUtils.setField(pComment, "content", "hello2");
            ReflectionTestUtils.setField(pComment, "parent", ppComment);

            final Comment comment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(comment, "id", 3L);
            ReflectionTestUtils.setField(comment, "content", "hello3");
            ReflectionTestUtils.setField(comment, "parent", pComment);

            given(commentRepository.findById(anyLong()))
                    .willReturn(Optional.of(pComment));

            assertThatThrownBy(() ->
                    commentValidationService.checkCommentMaxDepthOver(comment.getParent().getId()))
                    .isInstanceOf(CommentMaxDepthOverException.class);
        }
    }
}