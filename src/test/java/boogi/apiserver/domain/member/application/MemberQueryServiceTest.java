package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.NotViewableMemberException;
import boogi.apiserver.domain.member.vo.NullMember;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.response.UserJoinedCommunity;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberQueryServiceTest {

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    MemberQueryService memberQueryService;

    @Test
    @DisplayName("특정 유저가 가입한 멤버목록 조회")
    void myCommunityList() {

        //given
        User user = User.builder()
                .id(1L)
                .build();

        Community community1 = Community.builder()
                .id(2L)
                .communityName("커뮤니티1")
                .build();

        Community community2 = Community.builder()
                .id(3L)
                .communityName("커뮤니티2")
                .build();

        Member member1 = Member.builder()
                .id(4L)
                .user(user)
                .community(community1)
                .build();

        Member member2 = Member.builder()
                .id(5L)
                .user(user)
                .community(community2)
                .build();

        given(memberRepository.findByUserId(anyLong()))
                .willReturn(List.of(member1, member2));

        //when
        List<UserJoinedCommunity> dtos = memberQueryService.getJoinedMemberInfo(user.getId());

        //then
        UserJoinedCommunity dto1 = findUserJoinedCommunityById(dtos, 2L);
        assertThat(dto1.getName()).isEqualTo("커뮤니티1");

        UserJoinedCommunity dto2 = findUserJoinedCommunityById(dtos, 3L);
        assertThat(dto2.getName()).isEqualTo("커뮤니티2");
    }

    private UserJoinedCommunity findUserJoinedCommunityById(List<UserJoinedCommunity> dtos, Long id) {
        return dtos.stream().filter(d -> d.getId().equals(id)).findFirst().get();
    }

    @Nested
    @DisplayName("멤버 가입정보 조회 테스트")
    class MemberBasicInfo {

        @Test
        @DisplayName("가입정보 조회 성공")
        void success() {
            //given
            Member member = Member.builder().build();

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            //when
            Member memberOfTheCommunity = memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong());

            //then
            assertThat(memberOfTheCommunity).isEqualTo(member);
        }

        @Test
        @DisplayName("멤버 가입정보 없는 경우")
        void noMemberInfo() {
            //given
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.empty());

            //when
            Member memberOfTheCommunity = memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong());

            //then
            assertThat(memberOfTheCommunity).isEqualTo(null);
        }

    }

    @Test
    @DisplayName("특정 유저의 권한이 같은지 확인")
    void checkMyAuth() {
        Member member = Member.builder()
                .memberType(MemberType.MANAGER)
                .build();

        given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                .willReturn(Optional.of(member));

        Boolean hasAuth = memberQueryService.hasAuth(anyLong(), anyLong(), MemberType.MANAGER);

        assertThat(hasAuth).isTrue();
    }

    @Nested
    @DisplayName("커뮤니티의 내부를 열람 가능한 멤버를 조회할때")
    class GetViewableMemberTest {

        private final Community publicCommunity = Community.builder()
                .id(1L)
                .isPrivate(false)
                .build();

        private final Community privateCommunity = Community.builder()
                .id(1L)
                .isPrivate(true)
                .build();

        @Test
        @DisplayName("공개 커뮤니티에 가입된 멤버의 경우 해당 멤버를 가져온다.")
        void publicCommunityJoinedMemberSuccess() {
            Member member = Member.builder()
                    .id(2L)
                    .community(publicCommunity)
                    .build();

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            Member viewableMember = memberQueryService.getViewableMember(3L, publicCommunity);

            assertThat(viewableMember.getId()).isEqualTo(2L);
            assertThat(viewableMember.isJoined()).isTrue();
        }

        @Test
        @DisplayName("공개 커뮤니티에 가입하지 않은 멤버의 경우 NullMember를 가져온다.")
        void publicCommunityNotJoinedMemberSuccess() {
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.empty());

            Member viewableMember = memberQueryService.getViewableMember(2L, publicCommunity);

            assertThat(viewableMember).isEqualTo(new NullMember());
            assertThat(viewableMember.isJoined()).isFalse();
        }

        @Test
        @DisplayName("비공개 커뮤니티에 가입된 멤버의 경우 해당 멤버를 가져온다.")
        void privateCommunityJoinedMemberSuccess() {
            Member member = Member.builder()
                    .id(2L)
                    .community(privateCommunity)
                    .build();

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            Member viewableMember = memberQueryService.getViewableMember(3L, privateCommunity);

            assertThat(viewableMember.getId()).isEqualTo(2L);
            assertThat(viewableMember.isJoined()).isTrue();
        }

        @Test
        @DisplayName("비공개 커뮤니티에 가입되어있지 않는 멤버일 경우 NotViewableMemberException이 발생한다.")
        void privateCommunityNotJoinedMemberFail() {
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> memberQueryService.getViewableMember(2L, privateCommunity))
                    .isInstanceOf(NotViewableMemberException.class);
        }
    }
}