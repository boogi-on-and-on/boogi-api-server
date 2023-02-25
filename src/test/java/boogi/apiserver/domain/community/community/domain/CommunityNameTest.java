package boogi.apiserver.domain.community.community.domain;


import boogi.apiserver.domain.community.community.exception.InvalidCommunityNameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommunityNameTest {

    static Stream<Arguments> invalidRange() {
        return Stream.of(
                Arguments.of("A".repeat(31)),
                Arguments.of(" "),
                Arguments.of("")
        );
    }

    @DisplayName("범위 밖인 경우 에러")
    @ParameterizedTest
    @NullSource
    @MethodSource("invalidRange")
    void emptyString(String name) {
        assertThatThrownBy(() -> {
            new CommunityName(name);
        }).isInstanceOf(InvalidCommunityNameException.class);
    }

    @Test
    @DisplayName("생성 성공")
    void success() {
        final CommunityName communityName = new CommunityName("커뮤니티 이름");
        assertThat(communityName.getValue()).isEqualTo("커뮤니티 이름");
    }
}