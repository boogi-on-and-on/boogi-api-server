package boogi.apiserver.domain.community.joinrequest.application;

import boogi.apiserver.builder.TestMember;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.joinrequest.repository.JoinRequestRepository;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.community.joinrequest.exception.AlreadyRequestedException;
import boogi.apiserver.domain.member.application.MemberCommand;
import boogi.apiserver.domain.member.application.MemberQuery;
import boogi.apiserver.domain.member.repository.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.AlreadyJoinedMemberException;
import boogi.apiserver.domain.member.vo.NullMember;
import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.fixture.CommunityFixture;
import boogi.apiserver.utils.fixture.MemberFixture;
import boogi.apiserver.utils.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
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
class JoinRequestCommandTest {

    @InjectMocks
    JoinRequestCommand joinRequestCommand;

    @Mock
    JoinRequestRepository joinRequestRepository;

    @Mock
    MemberCommand memberCommand;


    @Mock
    CommunityRepository communityRepository;

    @Mock
    MemberQuery memberQuery;

    @Mock
    MemberRepository memberRepository;

    @Mock
    UserRepository userRepository;

    private User operatorUser;
    private Member operatorMember;
    private Community community;

    @BeforeEach
    public void init() {
        this.community = CommunityFixture.ENGLISH.toCommunity(1L, null);
        this.operatorUser = UserFixture.YONGJIN.toUser(2L);
        this.operatorMember = MemberFixture.YONGJIN_ENGLISH.toMember(3L, operatorUser, community);
    }

    @Nested
    @DisplayName("가입 요청하기")
    class RequestJoinRequest {

        @DisplayName("이미 가입한 경우")
        @Test
        void alreadyJoined() {
            //given
            given(memberQuery.getMemberOrNullMember(anyLong(), any(Community.class)))
                    .willReturn(operatorMember);

            given(communityRepository.findCommunityById(anyLong()))
                    .willReturn(community);

            //then
            assertThatThrownBy(() -> {
                //when
                joinRequestCommand.request(operatorUser.getId(), community.getId());
            }).isInstanceOf(AlreadyJoinedMemberException.class);
        }

        @DisplayName("이미 요청한 경우")
        @Test
        void alreadyRejected() {
            //given
            final JoinRequest joinRequest = JoinRequest.of(operatorUser, community);

            given(memberQuery.getMemberOrNullMember(anyLong(), any()))
                    .willReturn(new NullMember());

            given(joinRequestRepository.getLatestJoinRequest(anyLong(), anyLong()))
                    .willReturn(Optional.of(joinRequest));

            //then
            assertThatThrownBy(() -> {
                //when
                joinRequestCommand.request(operatorUser.getId(), community.getId());
            }).isInstanceOf(AlreadyRequestedException.class);
        }

        @DisplayName("커뮤니티 자동승인인 경우")
        @Test
        void success_autoApproval() {
            final Community autoCommunity = CommunityFixture.BASEBALL.toCommunity(1L, null);
            //given
            given(communityRepository.findCommunityById(anyLong()))
                    .willReturn(autoCommunity);

            given(memberQuery.getMemberOrNullMember(anyLong(), any()))
                    .willReturn(new NullMember());

            //when
            joinRequestCommand.request(operatorUser.getId(), autoCommunity.getId());

            //then
            then(memberRepository).should(times(1))
                    .save(any(Member.class));

            then(memberRepository).should(times(1))
                    .findManager(autoCommunity.getId());

            then(joinRequestRepository).should(times(1))
                    .save(any(JoinRequest.class));
        }

        @DisplayName("자동승인이 아닌 경우")
        @Test
        void success() {
            //given
            given(memberQuery.getMemberOrNullMember(anyLong(), any()))
                    .willReturn(new NullMember());

            given(communityRepository.findCommunityById(anyLong()))
                    .willReturn(community);

            //when
            joinRequestCommand.request(operatorUser.getId(), community.getId());

            //then
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
        given(memberQuery.getOperator(anyLong(), anyLong()))
                .willReturn(operatorMember);

        final User u1 = UserFixture.SUNDO.toUser(1L);
        final User u2 = UserFixture.DEOKHWAN.toUser(2L);

        JoinRequest jr1 = mock(JoinRequest.class);
        given(jr1.getUser()).willReturn(u1);

        JoinRequest jr2 = mock(JoinRequest.class);
        given(jr2.getUser()).willReturn(u2);

        given(joinRequestRepository.getRequestsByIds(any()))
                .willReturn(List.of(jr1, jr2));

        final Member m1 = TestMember.builder().user(u1).build();

        final Member m2 = TestMember.builder().user(u2).build();

        given(memberCommand.joinMembers(any(), anyLong(), any()))
                .willReturn(List.of(m1, m2));

        //when
        joinRequestCommand.confirmUsers(operatorMember.getId(), List.of(u1.getId(), u2.getId()), community.getId());

        //then
        then(jr1).should(times(1)).confirm(operatorMember, m1);
        then(jr2).should(times(1)).confirm(operatorMember, m2);
    }


    @Test
    @DisplayName("가입요청 여러개 거부하기")
    void rejectManyRequest() {
        //given
        given(memberQuery.getOperator(anyLong(), anyLong()))
                .willReturn(operatorMember);

        JoinRequest jr1 = mock(JoinRequest.class);
        JoinRequest jr2 = mock(JoinRequest.class);

        given(joinRequestRepository.getRequestsByIds(any()))
                .willReturn(List.of(jr1, jr2));
        //when
        joinRequestCommand.rejectUsers(operatorUser.getId(), List.of(1L, 2L), community.getId());

        //then
        then(jr1).should(times(1)).reject(operatorMember);
        then(jr2).should(times(1)).reject(operatorMember);
    }
}