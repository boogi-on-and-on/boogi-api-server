package boogi.apiserver.domain.post.post.domain;

import boogi.apiserver.domain.post.post.exception.InvalidPostContentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostContentTest {

    static Stream<Arguments> invalidRange() {
        return Stream.of(
                Arguments.of("A".repeat(9)),
                Arguments.of("A".repeat(1001)),
                Arguments.of(" ".repeat(10)),
                Arguments.of(" "),
                Arguments.of("")
        );
    }

    @DisplayName("입력값의 길이가 10 ~ 500 밖이거나 입력값이 Null이나 공백으로만 이루어진 문자열인 경우 실패")
    @ParameterizedTest(name = "{0}로 커뮤니티 설명 생성시 예외가 발생한다.")
    @NullSource
    @MethodSource("invalidRange")
    void createLengthFail(String content) {
        assertThatThrownBy(() -> new PostContent(content))
                .isInstanceOf(InvalidPostContentException.class);
    }

    @Test
    @DisplayName("입력값의 길이가 올바른 경우 성공")
    void createSuccess() {
        final PostContent postContent = new PostContent("게시글 내용입니다.");
        assertThat(postContent.getValue()).isEqualTo("게시글 내용입니다.");
    }
}