package boogi.apiserver.domain.community.joinrequest.application;

import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.joinrequest.dao.JoinRequestRepository;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequestStatus;
import boogi.apiserver.domain.member.application.MemberCoreService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JoinRequestCoreServiceTest {

    @InjectMocks
    JoinRequestCoreService joinRequestCoreService;

    @Mock
    JoinRequestRepository joinRequestRepository;

    @Mock
    MemberCoreService memberCoreService;

    @Mock
    JoinRequestQueryService joinRequestQueryService;

    @Mock
    UserQueryService userQueryService;

    @Mock
    CommunityQueryService communityQueryService;

    @Mock
    MemberQueryService memberQueryService;

    @Nested
    @DisplayName("가입 요청하기")
    class RequestJoinRequest {

        @Test
        @DisplayName("이미 요청한 경우")
        void alreadyRequested() {
            User user = User.builder().id(1L).build();
            given(userQueryService.getUser(anyLong()))
                    .willReturn(user);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);
            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            final JoinRequest joinRequest = TestEmptyEntityGenerator.JoinRequest();
            ReflectionTestUtils.setField(joinRequest, "status", JoinRequestStatus.PENDING);

            given(joinRequestRepository.getLatestJoinRequest(anyLong(), anyLong()))
                    .willReturn(Optional.of(joinRequest));

            assertThatThrownBy(() -> {
                joinRequestCoreService.request(user.getId(), community.getId());
            })
                    .isInstanceOf(InvalidValueException.class)
                    .hasMessage("이미 요청한 커뮤니티입니다.");
        }

        @Test
        @DisplayName("이미 가입한 경우")
        void alreadyJoined() {
            User user = User.builder().id(1L).build();
            given(userQueryService.getUser(anyLong()))
                    .willReturn(user);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            final JoinRequest joinRequest = TestEmptyEntityGenerator.JoinRequest();
            ReflectionTestUtils.setField(joinRequest, "status", JoinRequestStatus.CONFIRM);

            given(joinRequestRepository.getLatestJoinRequest(anyLong(), anyLong()))
                    .willReturn(Optional.of(joinRequest));

            assertThatThrownBy(() -> {
                joinRequestCoreService.request(user.getId(), community.getId());
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
            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 2L);

            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 3L);

            final JoinRequest joinRequest = TestEmptyEntityGenerator.JoinRequest();
            ReflectionTestUtils.setField(joinRequest, "id", 1L);
            ReflectionTestUtils.setField(joinRequest, "community", community);
            ReflectionTestUtils.setField(joinRequest, "user", user);

            given(joinRequestQueryService.getJoinRequest(anyLong()))
                    .willReturn(joinRequest);

            assertThatThrownBy(() -> {
                joinRequestCoreService.confirmUser(1L, 1L, 1L);
            }).isInstanceOf(InvalidValueException.class);
        }

        @Test
        @DisplayName("유저 여러개 승인하기")
        void confirmManyRequest() {
            //given
            User u1 = User.builder().id(1L).build();
            User u2 = User.builder().id(2L).build();

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 3L);

            JoinRequest jr1 = JoinRequest.of(u1, community);
            JoinRequest jr2 = JoinRequest.of(u2, community);

            given(joinRequestRepository.getRequestsByIds(any()))
                    .willReturn(List.of(jr1, jr2));

            Member m1 = Member.builder()
                    .id(1L)
                    .user(u1)
                    .community(community)
                    .build();

            Member m2 = Member.builder()
                    .id(2L)
                    .user(u2)
                    .community(community)
                    .build();
            given(memberCoreService.joinMemberInBatch(any(), anyLong(), any()))
                    .willReturn(List.of(m1, m2));

            Member manager = Member.builder()
                    .id(1L)
                    .build();
            given(memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong()))
                    .willReturn(manager);
            //when
            joinRequestCoreService.confirmUserInBatch(manager.getId(), List.of(1L, 2L), community.getId());

            //then
            assertThat(jr1.getStatus()).isEqualTo(JoinRequestStatus.CONFIRM);
            assertThat(jr1.getConfirmedMember()).isEqualTo(m1);
            assertThat(jr1.getAcceptor()).isEqualTo(manager);
        }

        @Test
        @DisplayName("여러개 승인 거부하기")
        void rejectManyRequest() {
            //given
            User u1 = User.builder().id(1L).build();
            User u2 = User.builder().id(2L).build();

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 3L);

            JoinRequest jr1 = JoinRequest.of(u1, community);
            JoinRequest jr2 = JoinRequest.of(u2, community);

            given(joinRequestRepository.getRequestsByIds(any()))
                    .willReturn(List.of(jr1, jr2));

            Member manager = Member.builder()
                    .id(1L)
                    .build();
            given(memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong()))
                    .willReturn(manager);

            //when
            joinRequestCoreService.rejectUserInBatch(anyLong(), any(), community.getId());
            assertThat(jr1.getStatus()).isEqualTo(JoinRequestStatus.REJECT);
            assertThat(jr2.getStatus()).isEqualTo(JoinRequestStatus.REJECT);
        }
    }
}