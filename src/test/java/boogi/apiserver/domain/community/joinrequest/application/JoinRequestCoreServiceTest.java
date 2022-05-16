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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

    @Test
    void 이미_요청한_경우() {
        User user = User.builder().id(1L).build();
        given(userQueryService.getUser(anyLong()))
                .willReturn(user);

        Community community = Community.builder().id(1L).build();
        given(communityQueryService.getCommunity(anyLong()))
                .willReturn(community);

        JoinRequest request = JoinRequest.builder()
                .status(JoinRequestStatus.PENDING)
                .build();
        given(joinRequestRepository.getLatestJoinRequest(anyLong(), anyLong()))
                .willReturn(request);

        assertThatThrownBy(() -> {
            joinRequestCoreService.request(user.getId(), community.getId());
        })
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("이미 요청한 커뮤니티입니다.");
    }

    @Test
    void 이미_가입한_경우() {
        User user = User.builder().id(1L).build();
        given(userQueryService.getUser(anyLong()))
                .willReturn(user);

        Community community = Community.builder().id(1L).build();
        given(communityQueryService.getCommunity(anyLong()))
                .willReturn(community);

        JoinRequest request = JoinRequest.builder()
                .status(JoinRequestStatus.CONFIRM)
                .build();
        given(joinRequestRepository.getLatestJoinRequest(anyLong(), anyLong()))
                .willReturn(request);

        assertThatThrownBy(() -> {
            joinRequestCoreService.request(user.getId(), community.getId());
        })
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("이미 가입한 커뮤니티입니다.");
    }

    @Test
    void 요청승인_id_매칭_실패() {
        JoinRequest request = JoinRequest.builder()
                .id(1L)
                .community(Community.builder().id(2L).build())
                .user(User.builder().id(3L).build())
                .build();

        given(joinRequestQueryService.getJoinRequest(anyLong()))
                .willReturn(request);

        assertThatThrownBy(() -> {
            joinRequestCoreService.confirmUser(1L, 1L, 1L);
        }).isInstanceOf(InvalidValueException.class);
    }


    @Test
    void 유저_여러개_승인() {
        //given
        User u1 = User.builder().id(1L).build();
        User u2 = User.builder().id(2L).build();

        Community community = Community.builder().id(3L).build();

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
    void 여러개_승인_거부() {
        //given
        User u1 = User.builder().id(1L).build();
        User u2 = User.builder().id(2L).build();

        Community community = Community.builder().id(3L).build();

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