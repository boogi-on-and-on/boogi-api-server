package boogi.apiserver.domain.community.joinrequest.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.joinrequest.dao.JoinRequestRepository;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequestStatus;
import boogi.apiserver.domain.member.application.MemberService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.user.dao.UserRepository;
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
class JoinRequestServiceTest {

    @InjectMocks
    JoinRequestService joinRequestService;

    @Mock
    JoinRequestRepository joinRequestRepository;

    @Mock
    MemberService memberService;


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
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            given(userRepository.findByUserId(anyLong()))
                    .willReturn(user);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);
            given(communityRepository.findByCommunityId(anyLong()))
                    .willReturn(community);

            final JoinRequest joinRequest = TestEmptyEntityGenerator.JoinRequest();
            ReflectionTestUtils.setField(joinRequest, "status", JoinRequestStatus.PENDING);

            given(joinRequestRepository.getLatestJoinRequest(anyLong(), anyLong()))
                    .willReturn(Optional.of(joinRequest));

            assertThatThrownBy(() -> {
                joinRequestService.request(user.getId(), community.getId());
            })
                    .isInstanceOf(InvalidValueException.class)
                    .hasMessage("이미 요청한 커뮤니티입니다.");
        }

        @Test
        @DisplayName("이미 가입한 경우")
        void alreadyJoined() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            given(userRepository.findByUserId(anyLong()))
                    .willReturn(user);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            given(communityRepository.findByCommunityId(anyLong()))
                    .willReturn(community);

            final JoinRequest joinRequest = TestEmptyEntityGenerator.JoinRequest();
            ReflectionTestUtils.setField(joinRequest, "status", JoinRequestStatus.CONFIRM);

            given(joinRequestRepository.getLatestJoinRequest(anyLong(), anyLong()))
                    .willReturn(Optional.of(joinRequest));

            assertThatThrownBy(() -> {
                joinRequestService.request(user.getId(), community.getId());
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

            given(joinRequestRepository.findByJoinRequestId(anyLong()))
                    .willReturn(joinRequest);

            assertThatThrownBy(() -> {
                joinRequestService.confirmUser(1L, 1L, 1L);
            }).isInstanceOf(InvalidValueException.class);
        }

        @Test
        @DisplayName("유저 여러개 승인하기")
        void confirmManyRequest() {
            //given
            final User u1 = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(u1, "id", 1L);
            final User u2 = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(u2, "id", 2L);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 3L);

            JoinRequest jr1 = JoinRequest.of(u1, community);
            JoinRequest jr2 = JoinRequest.of(u2, community);

            given(joinRequestRepository.getRequestsByIds(any()))
                    .willReturn(List.of(jr1, jr2));

            final Member m1 = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(m1, "id", 1L);
            ReflectionTestUtils.setField(m1, "user", u1);
            ReflectionTestUtils.setField(m1, "community", community);

            final Member m2 = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(m2, "id", 2L);
            ReflectionTestUtils.setField(m2, "user", u2);
            ReflectionTestUtils.setField(m2, "community", community);

            given(memberService.joinMemberInBatch(any(), anyLong(), any()))
                    .willReturn(List.of(m1, m2));

            final Member manager = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(manager, "id", 1L);

            given(memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong()))
                    .willReturn(manager);
            //when
            joinRequestService.confirmUserInBatch(manager.getId(), List.of(1L, 2L), community.getId());

            //then
            assertThat(jr1.getStatus()).isEqualTo(JoinRequestStatus.CONFIRM);
            assertThat(jr1.getConfirmedMember()).isEqualTo(m1);
            assertThat(jr1.getAcceptor()).isEqualTo(manager);
        }

        @Test
        @DisplayName("여러개 승인 거부하기")
        void rejectManyRequest() {
            //given
            final User u1 = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(u1, "id", 1L);
            final User u2 = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(u2, "id", 2L);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 3L);

            JoinRequest jr1 = JoinRequest.of(u1, community);
            JoinRequest jr2 = JoinRequest.of(u2, community);

            given(joinRequestRepository.getRequestsByIds(any()))
                    .willReturn(List.of(jr1, jr2));

            final Member manager = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(manager, "id", 1L);

            given(memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong()))
                    .willReturn(manager);

            //when
            joinRequestService.rejectUserInBatch(anyLong(), any(), community.getId());
            assertThat(jr1.getStatus()).isEqualTo(JoinRequestStatus.REJECT);
            assertThat(jr2.getStatus()).isEqualTo(JoinRequestStatus.REJECT);
        }
    }
}