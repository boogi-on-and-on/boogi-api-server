package boogi.apiserver.domain.user.domain;

import boogi.apiserver.domain.user.exception.InvalidUsernameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class UsernameTest {

    static Stream<Arguments> invalidLength() {
        return Stream.of(
                Arguments.of("A".repeat(1)),
                Arguments.of("A".repeat(21)),
                Arguments.of(""),
                Arguments.of(" ".repeat(5))
        );
    }

    @DisplayName("입력값의 길이가 2 ~ 20 밖이거나 입력값이 Null이나 공백으로만 이루어진 문자열인 경우 실패")
    @ParameterizedTest(name = "{0}로 유저 이름 생성시 예외가 발생한다.")
    @NullSource
    @MethodSource("invalidLength")
    void createLengthFail(String username) {
        assertThatThrownBy(() -> new Username(username))
                .isInstanceOf(InvalidUsernameException.class);
    }

    @DisplayName("자음이나 모음으로만 이루어지지 않는 한글 문자열이 아닌 경우 실패")
    @ParameterizedTest(name = "{0}로 유저 이름 생성시 예외가 발생한다.")
    @CsvSource({"abcd", "헤헤12", "han한", "123", "ㄱㄴㄷ"})
    void createWrongFormatFail(String username) {
        assertThatThrownBy(() -> new Username(username))
                .isInstanceOf(InvalidUsernameException.class);
    }

    @Test
    @DisplayName("입력값의 앞뒤 공백 문자를 제거")
    void trimSuccess() {
        String BEFORE_TRIM = "      유저이름    ";
        Username username = new Username(BEFORE_TRIM);
        assertThat(username.getValue()).isEqualTo(BEFORE_TRIM.trim());
    }

    @Test
    @DisplayName("입력값의 길이가 올바르고 공백으로만 이루어진 문자열이 아니면 성공")
    void createSuccess() {
        Username username = new Username("유저이름");
        assertThat(username.getValue()).isEqualTo("유저이름");
    }
}