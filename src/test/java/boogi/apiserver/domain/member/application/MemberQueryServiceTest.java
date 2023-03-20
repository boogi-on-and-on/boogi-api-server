package boogi.apiserver.domain.member.application;

import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.member.exception.NotViewableMemberException;
import boogi.apiserver.domain.member.vo.NullMember;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.dto.UserJoinedCommunityDto;
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
class MemberQueryServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    CommunityRepository communityRepository;

    @InjectMocks
    MemberQueryService memberQueryService;


    @Test
    @DisplayName("가입하지 않은 멤버 조회시 NotJoinedMemberException")
    void getMemberWithException() {
        given(memberRepository.findByUserIdAndCommunityId(any(), anyLong()))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> {
            memberQueryService.getMember(1L, 1L);
        }).isInstanceOf(NotJoinedMemberException.class);
    }

    @Test
    @DisplayName("특정 유저가 가입한 멤버목록 조회")
    void myCommunityList() {

        //given
        final User user = TestUser.builder().id(1L).build();

        final Community community1 = TestCommunity.builder()
                .id(2L)
                .communityName("커뮤니티A")
                .build();

        final Community community2 = TestCommunity.builder()
                .id(3L)
                .communityName("커뮤니티B")
                .build();

        final Member member1 = TestMember.builder()
                .id(4L)
                .user(user)
                .community(community1)
                .build();

        final Member member2 = TestMember.builder()
                .id(5L)
                .user(user)
                .community(community2)
                .build();

        given(memberRepository.findByUserId(anyLong()))
                .willReturn(List.of(member1, member2));

        //when
        List<UserJoinedCommunityDto> dtos = memberQueryService.getJoinedMemberInfo(user.getId());

        //then
        UserJoinedCommunityDto dto1 = findUserJoinedCommunityById(dtos, 2L);
        assertThat(dto1.getName()).isEqualTo("커뮤니티A");

        UserJoinedCommunityDto dto2 = findUserJoinedCommunityById(dtos, 3L);
        assertThat(dto2.getName()).isEqualTo("커뮤니티B");
    }

    private UserJoinedCommunityDto findUserJoinedCommunityById(List<UserJoinedCommunityDto> dtos, Long id) {
        return dtos.stream().filter(d -> d.getId().equals(id)).findFirst().get();
    }

    @Nested
    @DisplayName("멤버 가입정보 조회 테스트")
    class MemberBasicInfo {

        @Test
        @DisplayName("가입정보 조회 성공")
        void success() {
            //given
            final Member member = TestMember.builder().build();

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            //when
            Member memberOfTheCommunity = memberQueryService.getMember(1L, 1L);

            //then
            assertThat(memberOfTheCommunity).isEqualTo(member);
        }
    }

    @Nested
    @DisplayName("커뮤니티의 내부를 열람 가능한 멤버를 조회할때")
    class GetViewableMemberTest {

        private final Community publicCommunity = TestCommunity.builder()
                .id(1L)
                .isPrivate(false)
                .build();

        private final Community privateCommunity = TestCommunity.builder()
                .id(1L)
                .isPrivate(true)
                .build();

        @Test
        @DisplayName("공개 커뮤니티에 가입된 멤버의 경우 해당 멤버를 가져온다.")
        void publicCommunityJoinedMemberSuccess() {
            final Member member = TestMember.builder()
                    .id(2L)
                    .community(publicCommunity)
                    .build();

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            Member viewableMember = memberQueryService.getViewableMember(3L, publicCommunity);

            assertThat(viewableMember.getId()).isEqualTo(2L);
            assertThat(viewableMember.isNullMember()).isFalse();
        }

        @Test
        @DisplayName("공개 커뮤니티에 가입하지 않은 멤버의 경우 NullMember를 가져온다.")
        void publicCommunityNotJoinedMemberSuccess() {
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.empty());

            Member viewableMember = memberQueryService.getViewableMember(2L, publicCommunity);

            assertThat(viewableMember).isEqualTo(new NullMember());
            assertThat(viewableMember.isNullMember()).isTrue();
        }

        @Test
        @DisplayName("비공개 커뮤니티에 가입된 멤버의 경우 해당 멤버를 가져온다.")
        void privateCommunityJoinedMemberSuccess() {
            final Member member = TestMember.builder()
                    .id(2L)
                    .community(privateCommunity)
                    .build();

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            Member viewableMember = memberQueryService.getViewableMember(3L, privateCommunity);

            assertThat(viewableMember.getId()).isEqualTo(2L);
            assertThat(viewableMember.isNullMember()).isFalse();
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