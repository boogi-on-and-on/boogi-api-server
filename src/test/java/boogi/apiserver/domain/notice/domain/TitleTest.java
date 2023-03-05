package boogi.apiserver.domain.notice.domain;

import boogi.apiserver.domain.notice.exception.InvalidTitleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class TitleTest {

    static Stream<Arguments> invalidLength() {
        return Stream.of(
                Arguments.of("A".repeat(4)),
                Arguments.of("A".repeat(31)),
                Arguments.of(""),
                Arguments.of(" ".repeat(5))
        );
    }

    @DisplayName("입력값의 길이가 5 ~ 30 밖이거나 입력값이 Null이나 공백으로만 이루어진 문자열인 경우 실패")
    @ParameterizedTest(name = "{0}로 공지사항 제목 생성시 예외가 발생한다.")
    @NullSource
    @MethodSource("invalidLength")
    void createLengthFail(String title) {
        assertThatThrownBy(() -> new Title(title))
                .isInstanceOf(InvalidTitleException.class);
    }

    @Test
    @DisplayName("입력값의 길이가 올바르고 공백으로만 이루어진 문자열이 아니면 성공")
    void createSuccess() {
        Title title = new Title("공지사항 제목");
        assertThat(title.getValue()).isEqualTo("공지사항 제목");
    }
}