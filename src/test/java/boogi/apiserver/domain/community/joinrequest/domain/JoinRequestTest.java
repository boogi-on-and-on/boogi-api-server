package boogi.apiserver.domain.community.joinrequest.domain;


import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.joinrequest.exception.NotPendingJoinRequestException;
import boogi.apiserver.domain.community.joinrequest.exception.UnmatchedJoinRequestCommunityException;
import boogi.apiserver.domain.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JoinRequestTest {

    @Nested
    @DisplayName("가입요청 거절 테스트")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class RejectJoinRequest {
        Stream<Arguments> joinRequestStatusEnum() {
            final JoinRequest jr1 = JoinRequest.builder()
                    .status(JoinRequestStatus.REJECT)
                    .build();
            final JoinRequest jr2 = JoinRequest.builder()
                    .status(JoinRequestStatus.CONFIRM)
                    .build();
            return Stream.of(
                    Arguments.of(jr1),
                    Arguments.of(jr2)
            );
        }

        @DisplayName("Pending 상태가 아닌경우 NotPendingJoinRequestException리턴")
        @MethodSource("joinRequestStatusEnum")
        @ParameterizedTest
        void exception(JoinRequest joinRequest) {
            assertThatThrownBy(() -> {
                final Member member = TestMember.builder().build();
                joinRequest.reject(member);
            }).isInstanceOf(NotPendingJoinRequestException.class);
        }

        @DisplayName("성공")
        @Test
        void fail() {
            final JoinRequest joinRequest = JoinRequest.builder()
                    .status(JoinRequestStatus.PENDING)
                    .build();

            final Member manager = TestMember.builder().build();
            joinRequest.reject(manager);

            assertThat(joinRequest.getStatus()).isEqualTo(JoinRequestStatus.REJECT);
            assertThat(joinRequest.getAcceptor()).isEqualTo(manager);
        }
    }

    @Nested
    @DisplayName("가입요청 승인 테스트")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ConfirmJoinRequest {
        Stream<Arguments> joinRequestStatusEnum() {
            final JoinRequest jr1 = JoinRequest.builder()
                    .status(JoinRequestStatus.REJECT)
                    .build();
            final JoinRequest jr2 = JoinRequest.builder()
                    .status(JoinRequestStatus.CONFIRM)
                    .build();
            return Stream.of(
                    Arguments.of(jr1),
                    Arguments.of(jr2)
            );
        }

        @DisplayName("Pending 상태가 아닌경우 NotPendingJoinRequestException리턴")
        @MethodSource("joinRequestStatusEnum")
        @ParameterizedTest
        void exception(JoinRequest joinRequest) {
            assertThatThrownBy(() -> {
                final Member manager = TestMember.builder().build();
                final Member confirmedMember = TestMember.builder().build();
                joinRequest.confirm(manager, confirmedMember);
            }).isInstanceOf(NotPendingJoinRequestException.class);
        }


        @DisplayName("성공")
        @Test
        void success() {
            final JoinRequest joinRequest = JoinRequest.builder()
                    .status(JoinRequestStatus.PENDING)
                    .build();

            final Member manager = TestMember.builder().build();
            final Member confirmedMember = TestMember.builder().build();

            joinRequest.confirm(manager, confirmedMember);

            assertThat(joinRequest.getStatus()).isEqualTo(JoinRequestStatus.CONFIRM);
            assertThat(joinRequest.getAcceptor()).isEqualTo(manager);
            assertThat(joinRequest.getConfirmedMember()).isEqualTo(confirmedMember);
        }
    }

    @Test
    @DisplayName("JoinRequest.community와 파라미터 communityId가 일치하지 않으면 UnmatchedJoinRequestCommunityException")
    void validateJoinRequestCommunity() {
        final Community community = TestCommunity.builder()
                .id(1L)
                .build();

        final JoinRequest joinRequest = JoinRequest.builder()
                .community(community)
                .build();

        assertThatThrownBy(() -> {
            joinRequest.validateJoinRequestCommunity(2L);
        }).isInstanceOf(UnmatchedJoinRequestCommunityException.class);
    }
}