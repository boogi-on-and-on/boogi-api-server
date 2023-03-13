package boogi.apiserver.domain.community.community.domain;


import boogi.apiserver.domain.community.community.exception.InvalidDescriptionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DescriptionTest {

    static Stream<Arguments> invalidRange() {
        return Stream.of(
                Arguments.of("A".repeat(9)),
                Arguments.of("A".repeat(501)),
                Arguments.of(" ".repeat(10)),
                Arguments.of(" "),
                Arguments.of("")
        );
    }

    @DisplayName("입력값의 길이가 10 ~ 500 밖이거나 입력값이 Null이나 공백으로만 이루어진 문자열인 경우 실패")
    @ParameterizedTest(name = "{0}로 커뮤니티 설명 생성시 예외가 발생한다.")
    @NullSource
    @MethodSource("invalidRange")
    void createLengthFail(String description) {
        assertThatThrownBy(() -> new Description(description))
                .isInstanceOf(InvalidDescriptionException.class);
    }

    @Test
    @DisplayName("입력값의 앞뒤 공백 문자를 제거")
    void trimSuccess() {
        String BEFORE_TRIM = "      커뮤니티 소개란 입니다.    ";
        Description description = new Description(BEFORE_TRIM);
        assertThat(description.getValue()).isEqualTo(BEFORE_TRIM.trim());
    }

    @Test
    @DisplayName("입력값의 길이가 올바른 경우 성공")
    void createSuccess() {
        final Description description = new Description("커뮤니티 소개란 입니다.");
        assertThat(description.getValue()).isEqualTo("커뮤니티 소개란 입니다.");
    }
}