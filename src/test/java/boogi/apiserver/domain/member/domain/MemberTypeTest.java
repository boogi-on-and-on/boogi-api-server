package boogi.apiserver.domain.member.domain;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTypeTest {

    static Stream<Arguments> managerAuth() {
        return Stream.of(
                Arguments.of(MemberType.MANAGER, true),
                Arguments.of(MemberType.SUB_MANAGER, false),
                Arguments.of(MemberType.NORMAL, false)
        );
    }

    @DisplayName("매니저의 권한이 있는지 확인")
    @ParameterizedTest(name = "{0}인 경우 {1} 리턴")
    @MethodSource("managerAuth")
    void hasManagerAuth(MemberType type, boolean res) {
        assertThat(type.hasManagerAuth()).isEqualTo(res);
    }


    static Stream<Arguments> subManagerAuth() {
        return Stream.of(
                Arguments.of(MemberType.MANAGER, true),
                Arguments.of(MemberType.SUB_MANAGER, true),
                Arguments.of(MemberType.NORMAL, false)
        );
    }

    @DisplayName("부매니저의 권한이 있는지 확인")
    @ParameterizedTest(name = "{0}인 경우 {1} 리턴")
    @MethodSource("subManagerAuth")
    void hasSubManagerAuth(MemberType type, boolean res) {
        assertThat(type.hasSubManagerAuth()).isEqualTo(res);
    }
}