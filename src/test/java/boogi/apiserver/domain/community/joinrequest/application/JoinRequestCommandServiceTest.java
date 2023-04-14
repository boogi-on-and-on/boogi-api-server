package boogi.apiserver.domain.community.joinrequest.application;

import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.builder.TestJoinRequest;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.joinrequest.repository.JoinRequestRepository;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequestStatus;
import boogi.apiserver.domain.community.joinrequest.exception.AlreadyRequestedException;
import boogi.apiserver.domain.member.application.MemberCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.repository.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.AlreadyJoinedMemberException;
import boogi.apiserver.domain.member.vo.NullMember;
import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

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
    MemberRepository memberRepository;

    @Mock
    UserRepository userRepository;

    @Nested
    @DisplayName("가입 요청하기")
    class RequestJoinRequest {

        @DisplayName("이미 가입한 경우")
        @Test
        void alreadyJoined() {
            given(memberQueryService.getMemberOrNullMember(anyLong(), any(Community.class)))
                    .willReturn(TestMember.builder().build());

            given(communityRepository.findCommunityById(anyLong()))
                    .willReturn(TestCommunity.builder().id(1L).build());

            assertThatThrownBy(() -> {
                joinRequestCommandService.request(1L, 1L);
            }).isInstanceOf(AlreadyJoinedMemberException.class);
        }

        @DisplayName("이미 요청한 경우")
        @Test
        void alreadyRejected() {
            final JoinRequest joinRequest = TestJoinRequest.builder()
                    .status(JoinRequestStatus.PENDING)
                    .build();

            given(memberQueryService.getMemberOrNullMember(anyLong(), any()))
                    .willReturn(new NullMember());

            given(joinRequestRepository.getLatestJoinRequest(anyLong(), anyLong()))
                    .willReturn(Optional.of(joinRequest));

            assertThatThrownBy(() -> {
                joinRequestCommandService.request(1L, 1L);
            }).isInstanceOf(AlreadyRequestedException.class);
        }

        @DisplayName("커뮤니티 자동승인인 경우")
        @Test
        void success_autoApproval() {
            final Community community = TestCommunity.builder()
                    .id(1L)
                    .autoApproval(true)
                    .build();

            given(communityRepository.findCommunityById(anyLong()))
                    .willReturn(community);

            given(memberQueryService.getMemberOrNullMember(anyLong(), any()))
                    .willReturn(new NullMember());

            joinRequestCommandService.request(1L, community.getId());

            then(memberRepository).should(times(1))
                    .save(any(Member.class));

            then(memberRepository).should(times(1))
                    .findManager(community.getId());

            then(joinRequestRepository).should(times(1))
                    .save(any(JoinRequest.class));
        }

        @DisplayName("자동승인이 아닌 경우")
        @Test
        void success() {
            final Community community = TestCommunity.builder()
                    .id(1L)
                    .autoApproval(false)
                    .build();

            given(memberQueryService.getMemberOrNullMember(anyLong(), any()))
                    .willReturn(new NullMember());

            given(communityRepository.findCommunityById(anyLong()))
                    .willReturn(community);

            joinRequestCommandService.request(1L, community.getId());

            then(memberRepository).should(times(0))
                    .save(any(Member.class));

            then(memberRepository).should(times(0))
                    .findManager(community.getId());

            then(joinRequestRepository).should(times(1))
                    .save(any(JoinRequest.class));
        }
    }

    @Test
    @DisplayName("가입요청 여러개 승인하기")
    void confirmManyRequest() {
        //given
        final Member operator = TestMember.builder().id(1L).build();
        given(memberQueryService.getOperator(anyLong(), anyLong()))
                .willReturn(operator);

        final User u1 = TestUser.builder().id(1L).build();
        final User u2 = TestUser.builder().id(2L).build();


        JoinRequest jr1 = mock(JoinRequest.class);
        given(jr1.getUser()).willReturn(u1);

        JoinRequest jr2 = mock(JoinRequest.class);
        given(jr2.getUser()).willReturn(u2);

        given(joinRequestRepository.getRequestsByIds(any()))
                .willReturn(List.of(jr1, jr2));

        final Member m1 = TestMember.builder().user(u1).build();

        final Member m2 = TestMember.builder().user(u2).build();

        given(memberCommandService.joinMembers(any(), anyLong(), any()))
                .willReturn(List.of(m1, m2));

        //when
        joinRequestCommandService.confirmUsers(operator.getId(), List.of(1L, 2L), 1L);

        //then
        then(jr1).should(times(1)).confirm(operator, m1);
        then(jr2).should(times(1)).confirm(operator, m2);
    }


    @Test
    @DisplayName("가입요청 여러개 거부하기")
    void rejectManyRequest() {
        //given
        final Member operator = TestMember.builder().build();
        given(memberQueryService.getOperator(anyLong(), anyLong()))
                .willReturn(operator);

        final Community community = TestCommunity.builder().id(3L).build();

        JoinRequest jr1 = mock(JoinRequest.class);

        JoinRequest jr2 = mock(JoinRequest.class);

        given(joinRequestRepository.getRequestsByIds(any()))
                .willReturn(List.of(jr1, jr2));
        //when
        joinRequestCommandService.rejectUsers(1L, List.of(1L, 2L), community.getId());

        //then
        then(jr1).should(times(1)).reject(operator);
        then(jr2).should(times(1)).reject(operator);
    }
}