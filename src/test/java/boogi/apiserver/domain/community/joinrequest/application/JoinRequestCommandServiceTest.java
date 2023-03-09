package boogi.apiserver.domain.community.joinrequest.application;

import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.builder.TestJoinRequest;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.joinrequest.dao.JoinRequestRepository;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequestStatus;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.application.MemberCommandService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JoinRequestCommandServiceTest {

    @InjectMocks
    JoinRequestCommandService joinRequestCommandService;

    @Mock
    JoinRequestRepository joinRequestRepository;

    @Mock
    MemberCommandService memberCommandService;


    @Mock
    CommunityRepository communityRepository;

    @Mock
    MemberQueryService memberQueryService;

    @Mock
    UserRepository userRepository;

    @Nested
    @DisplayName("가입 요청하기")
    class RequestJoinRequest {

        @Test
        @DisplayName("이미 요청한 경우")
        void alreadyRequested() {
            final User user = TestUser.builder()
                    .id(1L)
                    .build();
            given(userRepository.findByUserId(anyLong()))
                    .willReturn(user);

            final Community community = TestCommunity.builder()
                    .id(1L)
                    .build();
            given(communityRepository.findByCommunityId(anyLong()))
                    .willReturn(community);

            final JoinRequest joinRequest = TestJoinRequest.builder()
                    .status(JoinRequestStatus.PENDING)
                    .build();

            given(joinRequestRepository.getLatestJoinRequest(anyLong(), anyLong()))
                    .willReturn(Optional.of(joinRequest));

            assertThatThrownBy(() -> {
                joinRequestCommandService.request(user.getId(), community.getId());
            })
                    .isInstanceOf(InvalidValueException.class)
                    .hasMessage("이미 요청한 커뮤니티입니다.");
        }

        @Test
        @DisplayName("이미 가입한 경우")
        void alreadyJoined() {
            final User user = TestUser.builder()
                    .id(1L)
                    .build();
            given(userRepository.findByUserId(anyLong()))
                    .willReturn(user);

            final Community community = TestCommunity.builder()
                    .id(1L)
                    .build();
            given(communityRepository.findByCommunityId(anyLong()))
                    .willReturn(community);

            final JoinRequest joinRequest = TestJoinRequest.builder()
                    .status(JoinRequestStatus.CONFIRM)
                    .build();

            given(joinRequestRepository.getLatestJoinRequest(anyLong(), anyLong()))
                    .willReturn(Optional.of(joinRequest));

            assertThatThrownBy(() -> {
                joinRequestCommandService.request(user.getId(), community.getId());
            })
                    .isInstanceOf(InvalidValueException.class)
                    .hasMessage("이미 가입한 커뮤니티입니다.");
        }
    }

    @Nested
    @DisplayName("가입 요청 승인 및 거부")
    class ConfirmOrRejectRequest {
        @Test
        @DisplayName("요청승인 id의 매칭 실패")
        void unmatch() {
            final Community community = TestCommunity.builder()
                    .id(2L)
                    .build();

            final User user = TestUser.builder()
                    .id(3L)
                    .build();

            final JoinRequest joinRequest = TestJoinRequest.builder()
                    .id(1L)
                    .community(community)
                    .user(user)
                    .build();

            given(joinRequestRepository.findByJoinRequestId(anyLong()))
                    .willReturn(joinRequest);

            assertThatThrownBy(() -> {
                joinRequestCommandService.confirmUser(1L, 1L, 1L);
            }).isInstanceOf(InvalidValueException.class);
        }

        @Test
        @DisplayName("유저 여러개 승인하기")
        void confirmManyRequest() {
            //given
            final User u1 = TestUser.builder()
                    .id(1L)
                    .build();

            final User u2 = TestUser.builder()
                    .id(2L)
                    .build();

            final Community community = TestCommunity.builder()
                    .id(3L)
                    .build();

            JoinRequest jr1 = JoinRequest.of(u1, community);
            JoinRequest jr2 = JoinRequest.of(u2, community);

            given(joinRequestRepository.getRequestsByIds(any()))
                    .willReturn(List.of(jr1, jr2));

            final Member m1 = TestMember.builder()
                    .id(1L)
                    .user(u1)
                    .community(community)
                    .build();

            final Member m2 = TestMember.builder()
                    .id(2L)
                    .user(u2)
                    .community(community)
                    .build();

            given(memberCommandService.joinMembers(any(), anyLong(), any()))
                    .willReturn(List.of(m1, m2));

            final Member manager = TestMember.builder().id(1L).build();

            given(memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong()))
                    .willReturn(manager);
            //when
            joinRequestCommandService.confirmUserInBatch(manager.getId(), List.of(1L, 2L), community.getId());

            //then
            assertThat(jr1.getStatus()).isEqualTo(JoinRequestStatus.CONFIRM);
            assertThat(jr1.getConfirmedMember()).isEqualTo(m1);
            assertThat(jr1.getAcceptor()).isEqualTo(manager);
        }

        @Test
        @DisplayName("여러개 승인 거부하기")
        void rejectManyRequest() {
            //given
            final User u1 = TestUser.builder().id(1L).build();
            final User u2 = TestUser.builder().id(2L).build();

            final Community community = TestCommunity.builder().id(3L).build();

            JoinRequest jr1 = JoinRequest.of(u1, community);
            JoinRequest jr2 = JoinRequest.of(u2, community);

            given(joinRequestRepository.getRequestsByIds(any()))
                    .willReturn(List.of(jr1, jr2));

            final Member manager = TestMember.builder().id(1L).build();

            given(memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong()))
                    .willReturn(manager);

            //when
            joinRequestCommandService.rejectUserInBatch(anyLong(), any(), community.getId());
            assertThat(jr1.getStatus()).isEqualTo(JoinRequestStatus.REJECT);
            assertThat(jr2.getStatus()).isEqualTo(JoinRequestStatus.REJECT);
        }
    }
}