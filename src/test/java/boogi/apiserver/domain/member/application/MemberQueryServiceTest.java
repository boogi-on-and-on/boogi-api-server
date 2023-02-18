package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.dto.dto.MemberDto;
import boogi.apiserver.domain.member.exception.NotViewableMemberException;
import boogi.apiserver.domain.member.vo.NullMember;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.dto.UserJoinedCommunityDto;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
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

    @Mock
    CommunityRepository communityRepository;

    @InjectMocks
    MemberQueryService memberQueryService;

    @Test
    @DisplayName("특정 유저가 가입한 멤버목록 조회")
    void myCommunityList() {

        //given
        final User user = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user, "id", 1L);

        final Community community1 = TestEmptyEntityGenerator.Community();
        ReflectionTestUtils.setField(community1, "id", 2L);
        ReflectionTestUtils.setField(community1, "communityName", "커뮤니티1");

        final Community community2 = TestEmptyEntityGenerator.Community();
        ReflectionTestUtils.setField(community2, "id", 3L);
        ReflectionTestUtils.setField(community2, "communityName", "커뮤니티2");

        final Member member1 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member1, "id", 4L);
        ReflectionTestUtils.setField(member1, "user", user);
        ReflectionTestUtils.setField(member1, "community", community1);

        final Member member2 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member2, "id", 4L);
        ReflectionTestUtils.setField(member2, "user", user);
        ReflectionTestUtils.setField(member2, "community", community2);

        given(memberRepository.findByUserId(anyLong()))
                .willReturn(List.of(member1, member2));

        //when
        List<UserJoinedCommunityDto> dtos = memberQueryService.getJoinedMemberInfo(user.getId());

        //then
        UserJoinedCommunityDto dto1 = findUserJoinedCommunityById(dtos, 2L);
        assertThat(dto1.getName()).isEqualTo("커뮤니티1");

        UserJoinedCommunityDto dto2 = findUserJoinedCommunityById(dtos, 3L);
        assertThat(dto2.getName()).isEqualTo("커뮤니티2");
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
            final Member member = TestEmptyEntityGenerator.Member();


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
        final Member member = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member, "memberType", MemberType.MANAGER);


        given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                .willReturn(Optional.of(member));

        Boolean hasAuth = memberQueryService.hasAuth(anyLong(), anyLong(), MemberType.MANAGER);

        assertThat(hasAuth).isTrue();
    }

    @Nested
    @DisplayName("커뮤니티의 내부를 열람 가능한 멤버를 조회할때")
    class GetViewableMemberTest {

        private final Community publicCommunity = TestEmptyEntityGenerator.Community();

        private final Community privateCommunity = TestEmptyEntityGenerator.Community();


        @BeforeEach
        void init() {
            ReflectionTestUtils.setField(publicCommunity, "id", 1L);
            ReflectionTestUtils.setField(publicCommunity, "isPrivate", false);

            ReflectionTestUtils.setField(privateCommunity, "id", 1L);
            ReflectionTestUtils.setField(privateCommunity, "isPrivate", true);
        }

        @Test
        @DisplayName("공개 커뮤니티에 가입된 멤버의 경우 해당 멤버를 가져온다.")
        void publicCommunityJoinedMemberSuccess() {
            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 2L);
            ReflectionTestUtils.setField(member, "community", publicCommunity);

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
            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 2L);
            ReflectionTestUtils.setField(member, "community", privateCommunity);

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