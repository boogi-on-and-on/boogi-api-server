package boogi.apiserver.domain.member.application;

import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.AlreadyJoinedMemberException;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;


@ExtendWith(MockitoExtension.class)
class MemberCommandServiceTest {

    @InjectMocks
    MemberCommandService memberCommandService;

    @Mock
    MemberRepository memberRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CommunityRepository communityRepository;

    @Mock
    MemberQueryService memberQueryService;


    @Nested
    @DisplayName("멤버 가입 테스트")
    class JoinMemberTest {

        @DisplayName("이미 가입한 멤버인 경우 AlreadyJoinedMemberException 리턴")
        @Test
        void alreadyJoined() {
            final Member member = TestMember.builder()
                    .user(
                            TestUser.builder()
                                    .id(1L)
                                    .build()
                    ).build();

            given(memberRepository.findAlreadyJoinedMember(anyList(), anyLong()))
                    .willReturn(List.of(member));

            assertThatThrownBy(() -> {
                memberCommandService.joinMember(member.getUser().getId(), 1L, MemberType.NORMAL);
            }).isInstanceOf(AlreadyJoinedMemberException.class);
        }

        @DisplayName("가입 성공")
        @Test
        void success() {
            final User user = TestUser.builder().build();
            given(userRepository.findUserById(anyLong()))
                    .willReturn(user);

            final Community community = TestCommunity.builder().build();
            given(communityRepository.findCommunityById(anyLong()))
                    .willReturn(community);

            final Member member = memberCommandService.joinMember(1L, 1L, MemberType.NORMAL);

            then(memberRepository).should(times(1))
                    .save(any(Member.class));
        }
    }

    @Nested
    @DisplayName("멤버 여러명 추가하기")
    class JoinManyMembers {

        @DisplayName("이미 가입한 멤버가 있는경우 AlreadyJoinedMemberException")
        @Test
        void alreadyJoined() {
            final Member member = TestMember.builder()
                    .user(
                            TestUser.builder()
                                    .id(1L)
                                    .build()
                    ).build();

            given(memberRepository.findAlreadyJoinedMember(anyList(), anyLong()))
                    .willReturn(List.of(member));

            assertThatThrownBy(() -> {
                memberCommandService.joinMembers(List.of(1L, 2L), 1L, MemberType.NORMAL);
            }).isInstanceOf(AlreadyJoinedMemberException.class);
        }

        @DisplayName("성공")
        @Test
        void success() {
            final Community community = TestCommunity.builder().build();
            given(communityRepository.findCommunityById(anyLong()))
                    .willReturn(community);

            memberCommandService.joinMembers(List.of(1L, 2L), 3L, MemberType.NORMAL);
            then(memberRepository).should(times(1)).saveAll(any(List.class));
        }
    }

    @Test
    @DisplayName("멤버 차단 테스트")
    void ban() {

        final Member member = mock(Member.class);

        final Community community = TestCommunity.builder().id(1L).build();
        given(member.getCommunity()).willReturn(community);

        given(memberRepository.findMemberById(anyLong()))
                .willReturn(member);

        memberCommandService.banMember(2L, 1L);

        then(member).should(times(1)).ban();
    }

    @Test
    @DisplayName("멤버 차단해제 테스트")
    void release() {
        final Member member = mock(Member.class);

        final Community community = TestCommunity.builder().id(1L).build();
        given(member.getCommunity()).willReturn(community);

        given(memberRepository.findMemberById(anyLong()))
                .willReturn(member);

        memberCommandService.releaseMember(2L, 1L);

        then(member).should(times(1)).release();
    }

    @Nested
    @DisplayName("멤버 권한 위임 테스트")
    class DelegateMemberTest {

        @DisplayName("매니저의 권한을 위임할 경우 매니저와 멤버 둘 다 변한다")
        @Test
        void managerAndNormalMemberType() {
            final Member member = mock(Member.class);

            final Community community = TestCommunity.builder().id(1L).build();
            given(member.getCommunity()).willReturn(community);

            given(memberRepository.findMemberById(anyLong()))
                    .willReturn(member);

            final Member manager = mock(Member.class);

            given(memberQueryService.getManager(anyLong(), anyLong()))
                    .willReturn(manager);

            memberCommandService.delegateMember(2L, 1L, MemberType.MANAGER);

            then(manager).should(times(1)).changeMemberType(MemberType.NORMAL);
            then(member).should(times(1)).changeMemberType(MemberType.MANAGER);
        }

        @DisplayName("일반 멤버의 권한을 바꿔주는 경우 일반멤버의 권한만 변한다.")
        @Test
        void normalMemberType() {
            final Member member = mock(Member.class);

            final Community community = TestCommunity.builder().id(1L).build();
            given(member.getCommunity()).willReturn(community);

            given(memberRepository.findMemberById(anyLong()))
                    .willReturn(member);

            final Member manager = mock(Member.class);

            given(memberQueryService.getManager(anyLong(), anyLong()))
                    .willReturn(manager);

            memberCommandService.delegateMember(2L, 1L, MemberType.NORMAL);

            then(manager).should(times(0)).changeMemberType(MemberType.NORMAL);
            then(member).should(times(1)).changeMemberType(MemberType.NORMAL);
        }
    }
}