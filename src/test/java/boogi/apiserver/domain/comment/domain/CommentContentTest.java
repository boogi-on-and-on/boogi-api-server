package boogi.apiserver.domain.comment.domain;

import boogi.apiserver.domain.comment.exception.InvalidCommentContentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class CommentContentTest {

    static Stream<Arguments> invalidLength() {
        return Stream.of(
                Arguments.of("A".repeat(256)),
                Arguments.of(""),
                Arguments.of("        ")
        );
    }

    @DisplayName("입력값의 길이가 1 ~ 255 밖이거나 입력값이 Null이나 공백으로만 이루어진 문자열인 경우 실패")
    @ParameterizedTest(name = "{0}로 댓글 내용 생성시 예외가 발생한다.")
    @NullSource
    @MethodSource("invalidLength")
    void createLengthFail(String content) {
        assertThatThrownBy(() -> new CommentContent(content))
                .isInstanceOf(InvalidCommentContentException.class);
    }

    @Test
    @DisplayName("입력값의 앞뒤 공백 문자를 제거")
    void trimSuccess() {
        String BEFORE_TRIM = "      댓글 내용입니다.    ";
        CommentContent commentContent = new CommentContent(BEFORE_TRIM);
        assertThat(commentContent.getValue()).isEqualTo(BEFORE_TRIM.trim());
    }

    @Test
    @DisplayName("입력값의 길이가 올바르고 공백으로만 이루어진 문자열이 아니면 성공")
    void createSuccess() {
        CommentContent commentContent = new CommentContent("댓글 내용");
        assertThat(commentContent.getValue()).isEqualTo("댓글 내용");
    }
}