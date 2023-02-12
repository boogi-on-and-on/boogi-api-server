package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.response.UserJoinedCommunity;
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
}