package boogi.apiserver.domain.user.domain;

import boogi.apiserver.domain.user.exception.InvalidIntroduceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class IntroduceTest {

    static Stream<Arguments> invalidLength() {
        return Stream.of(
                Arguments.of("A".repeat(9)),
                Arguments.of("A".repeat(501)),
                Arguments.of(""),
                Arguments.of(" ".repeat(10))
        );
    }

    @DisplayName("입력값의 길이가 10 ~ 500 밖이거나 입력값이 Null이나 공백으로만 이루어진 문자열인 경우 실패")
    @ParameterizedTest(name = "{0}로 소개글 생성시 예외가 발생한다.")
    @NullSource
    @MethodSource("invalidLength")
    void createLengthFail(String introduce) {
        assertThatThrownBy(() -> new Introduce(introduce))
                .isInstanceOf(InvalidIntroduceException.class);
    }

    @Test
    @DisplayName("입력값의 앞뒤 공백 문자를 제거")
    void trimSuccess() {
        String BEFORE_TRIM = "      유저 소개글입니다.    ";
        Introduce introduce = new Introduce(BEFORE_TRIM);
        assertThat(introduce.getValue()).isEqualTo(BEFORE_TRIM.trim());
    }

    @Test
    @DisplayName("입력값의 길이가 올바르고 공백으로만 이루어진 문자열이 아니면 성공")
    void createSuccess() {
        Introduce introduce = new Introduce("안녕하세요. 소개글입니다.");
        assertThat(introduce.getValue()).isEqualTo("안녕하세요. 소개글입니다.");
    }
}