package boogi.apiserver.domain.member.domain;

import boogi.apiserver.builder.TestMember;
import boogi.apiserver.domain.member.exception.NotBannedMemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @Nested
    @DisplayName("멤버 차단 테스트")
    class Ban {
        @Test
        @DisplayName("이미 차단된 경우 업데이트 X")
        void alreadyBanned() {
            final Member member = TestMember.builder()
                    .bannedAt(LocalDateTime.now())
                    .build();

            member.ban();

            assertThat(member.getBannedAt()).isEqualTo(member.getBannedAt());
        }

        @Test
        @DisplayName("차단 시 시간 업데이트")
        void success() {
            final Member member = TestMember.builder()
                    .build();

            member.ban();

            assertThat(member.getBannedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("멤버 차단 해제 테스트")
    class Release {

        @Test
        @DisplayName("차단이 안됐는데 해제할 경우 NotBannedMemberException")
        void exception() {
            final Member member = TestMember.builder()
                    .build();

            assertThatThrownBy(member::release).isInstanceOf(NotBannedMemberException.class);
        }

        @Test
        @DisplayName("성공")
        void success() {
            final Member member = TestMember.builder()
                    .bannedAt(LocalDateTime.now())
                    .build();

            member.release();

            assertThat(member.getBannedAt()).isNull();
        }
    }
}