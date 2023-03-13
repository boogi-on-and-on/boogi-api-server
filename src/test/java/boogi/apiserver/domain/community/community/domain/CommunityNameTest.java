package boogi.apiserver.domain.community.community.domain;


import boogi.apiserver.domain.community.community.exception.InvalidCommunityNameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommunityNameTest {

    static Stream<Arguments> invalidRange() {
        return Stream.of(
                Arguments.of("A".repeat(31)),
                Arguments.of(""),
                Arguments.of("    ")
        );
    }

    @DisplayName("입력값의 길이가 1 ~ 30 밖이거나 입력값이 Null이나 공백으로만 이루어진 문자열인 경우 실패")
    @ParameterizedTest(name = "{0}로 커뮤니티 이름 생성시 예외가 발생한다.")
    @NullSource
    @MethodSource("invalidRange")
    void createLengthFail(String name) {
        assertThatThrownBy(() -> new CommunityName(name))
                .isInstanceOf(InvalidCommunityNameException.class);
    }

    @DisplayName("최소 1개의 한글이나 영어와 공백문자로만 구성되어 있지 않을 경우 실패")
    @ParameterizedTest(name = "{0}로 커뮤니티 이름 생성시 예외가 발생한다.")
    @CsvSource({"1234", "!@$%", "안녕 123", "hello!", "안녕hello~"})
    void wrongLetterFail(String name) {
        assertThatThrownBy(() -> new CommunityName(name))
                .isInstanceOf(InvalidCommunityNameException.class);
    }

    @Test
    @DisplayName("입력값의 앞뒤 공백 문자를 제거")
    void trimSuccess() {
        String BEFORE_TRIM = "      커뮤니티 이름    ";
        CommunityName communityName = new CommunityName(BEFORE_TRIM);
        assertThat(communityName.getValue()).isEqualTo(BEFORE_TRIM.trim());
    }

    @DisplayName("입력값의 길이가 올바르고, 최소 1개 이상의 한글이나 영어와 공백문자로만 구성되면 성공")
    @ParameterizedTest(name = "{0}로 커뮤니티 이름을 생성한다.")
    @CsvSource({"커뮤니티 이름", "community name", "community 이름", "communityName"})
    void createSuccess(String name) {
        final CommunityName communityName = new CommunityName(name);
        assertThat(communityName.getValue()).isEqualTo(name);
    }
}