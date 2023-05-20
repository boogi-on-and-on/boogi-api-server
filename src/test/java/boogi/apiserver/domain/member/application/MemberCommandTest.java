package boogi.apiserver.domain.member.application;

import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.AlreadyJoinedMemberException;
import boogi.apiserver.domain.member.repository.MemberRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;


@ExtendWith(MockitoExtension.class)
class MemberCommandTest {

    @InjectMocks
    MemberCommand memberCommand;

    @Mock
    MemberRepository memberRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CommunityRepository communityRepository;

    @Mock
    MemberQuery memberQuery;

    private Member manager;
    private Member normal;
    private Community community;
    private User newUser;

    @BeforeEach
    public void init() {
        this.community = CommunityFixture.POCS.toCommunity(1L, null);

        final User sundo = UserFixture.SUNDO.toUser(2L);
        this.manager = MemberFixture.SUNDO_POCS.toMember(3L, sundo, community);

        final User deokhwan = UserFixture.DEOKHWAN.toUser(4L);
        this.normal = MemberFixture.DEOKHWAN_POCS.toMember(5L, deokhwan, community);

        this.newUser = UserFixture.YONGJIN.toUser(6L);
    }

    @Nested
    @DisplayName("멤버 가입 테스트")
    class JoinMemberTest {

        @DisplayName("이미 가입한 멤버인 경우 AlreadyJoinedMemberException 리턴")
        @Test
        void alreadyJoined() {
            //given
            given(memberRepository.findAlreadyJoinedMember(anyList(), anyLong()))
                    .willReturn(List.of(manager));

            //then
            assertThatThrownBy(() -> {
                //when
                memberCommand.joinMember(manager.getUser().getId(), community.getId(), MemberType.NORMAL);
            }).isInstanceOf(AlreadyJoinedMemberException.class);
        }

        @DisplayName("가입 성공")
        @Test
        void success() {
            //given
            given(userRepository.findUserById(anyLong()))
                    .willReturn(newUser);
            given(communityRepository.findCommunityById(anyLong()))
                    .willReturn(community);

            //when
            final Member member = memberCommand.joinMember(newUser.getId(), community.getId(), MemberType.NORMAL);

            //then
            assertThat(member.getMemberType()).isEqualTo(MemberType.NORMAL);
            assertThat(member.getUser()).isEqualTo(newUser);

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
            given(memberRepository.findAlreadyJoinedMember(anyList(), anyLong()))
                    .willReturn(List.of(manager));

            assertThatThrownBy(() -> {
                memberCommand.joinMembers(List.of(1L, 2L), community.getId(), MemberType.NORMAL);
            }).isInstanceOf(AlreadyJoinedMemberException.class);
        }

        @DisplayName("성공")
        @Test
        void success() {
            final Community community = TestCommunity.builder().build();
            given(communityRepository.findCommunityById(anyLong()))
                    .willReturn(community);

            memberCommand.joinMembers(List.of(1L, 2L), 3L, MemberType.NORMAL);
            then(memberRepository).should(times(1)).saveAll(any(List.class));
        }
    }

    @Test
    @DisplayName("멤버 차단 테스트")
    void ban() {
        //given
        given(memberRepository.findMemberById(anyLong()))
                .willReturn(manager);

        //when
        memberCommand.banMember(2L, manager.getId());

        //then
        assertThat(manager.getBannedAt()).isNotNull();
    }

    @Test
    @DisplayName("멤버 차단해제 테스트")
    void release() {
        //given
        final Member member = MemberFixture.DEOKHWAN_ENGLISH_BANNED.toMember(newUser, community);
        given(memberRepository.findMemberById(anyLong()))
                .willReturn(member);

        //when
        memberCommand.releaseMember(2L, 1L);

        //then
        assertThat(member.getBannedAt()).isNull();
    }

    @Nested
    @DisplayName("멤버 권한 위임 테스트")
    class DelegateMemberTest {

        @DisplayName("매니저의 권한을 위임할 경우 매니저와 멤버 둘 다 변한다")
        @Test
        void managerAndNormalMemberType() {
            //given
            given(memberRepository.findMemberById(anyLong()))
                    .willReturn(normal);

            given(memberQuery.getManager(anyLong(), anyLong()))
                    .willReturn(manager);

            //when
            memberCommand.delegateMember(2L, 1L, MemberType.MANAGER);

            //then
            assertThat(manager.getMemberType()).isEqualTo(MemberType.NORMAL);
            assertThat(normal.getMemberType()).isEqualTo(MemberType.MANAGER);
        }

        @DisplayName("일반 멤버의 권한을 바꿔주는 경우 일반멤버의 권한만 변한다.")
        @Test
        void normalMemberType() {
            //given
            given(memberRepository.findMemberById(anyLong()))
                    .willReturn(normal);

            given(memberQuery.getManager(anyLong(), anyLong()))
                    .willReturn(manager);
            //when
            memberCommand.delegateMember(2L, 1L, MemberType.NORMAL);

            //then
            assertThat(manager.getMemberType()).isEqualTo(MemberType.MANAGER);
            assertThat(normal.getMemberType()).isEqualTo(MemberType.NORMAL);
        }
    }
}