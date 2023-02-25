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
                Arguments.of(" "),
                Arguments.of("")
        );
    }

    @DisplayName("범위 밖인 경우 에러")
    @ParameterizedTest
    @NullSource
    @MethodSource("invalidRange")
    void emptyString(String description) {
        assertThatThrownBy(() -> {
            new Description(description);
        }).isInstanceOf(InvalidDescriptionException.class);
    }

    @Test
    @DisplayName("생성 성공")
    void success() {
        final Description description = new Description("커뮤니티 소개란 입니다.");
        assertThat(description.getValue()).isEqualTo("커뮤니티 소개란 입니다.");
    }
}