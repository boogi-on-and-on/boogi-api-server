package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.member.exception.NotViewableMemberException;
import boogi.apiserver.domain.member.repository.MemberRepository;
import boogi.apiserver.domain.member.vo.NullMember;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.dto.UserJoinedCommunityDto;
import boogi.apiserver.utils.fixture.MemberFixture;
import boogi.apiserver.utils.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static boogi.apiserver.utils.fixture.CommunityFixture.ENGLISH;
import static boogi.apiserver.utils.fixture.CommunityFixture.POCS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberQueryTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    CommunityRepository communityRepository;

    @InjectMocks
    MemberQuery memberQuery;

    private final User user = UserFixture.YONGJIN.toUser(1L);
    private final Community community1 = POCS.toCommunity(2L, null);
    private final Community community2 = ENGLISH.toCommunity(3L, null);
    private final Member member1 = MemberFixture.YONGJIN_POCS.toMember(4L, user, community1);
    private final Member member2 = MemberFixture.YONGJIN_ENGLISH.toMember(5L, user, community2);

    @Test
    @DisplayName("가입하지 않은 멤버 조회시 NotJoinedMemberException")
    void getMemberWithException() {
        //given
        given(memberRepository.findByUserIdAndCommunityId(any(), anyLong()))
                .willReturn(Optional.empty());

        //then
        assertThatThrownBy(() -> {
            //when
            memberQuery.getMember(user.getId(), community1.getId());
        }).isInstanceOf(NotJoinedMemberException.class);
    }

    @Test
    @DisplayName("특정 유저가 가입한 멤버목록 조회")
    void myCommunityList() {

        //given
        given(memberRepository.findByUserId(anyLong()))
                .willReturn(List.of(member1, member2));

        //when
        List<UserJoinedCommunityDto> dtos = memberQuery.getJoinedMemberInfo(user.getId());

        //then
        UserJoinedCommunityDto dto1 = findUserJoinedCommunityById(dtos, community1.getId());
        assertThat(dto1.getName()).isEqualTo(POCS.communityName);

        UserJoinedCommunityDto dto2 = findUserJoinedCommunityById(dtos, community2.getId());
        assertThat(dto2.getName()).isEqualTo(ENGLISH.communityName);
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
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member1));

            //when
            Member memberOfTheCommunity = memberQuery.getMember(user.getId(), community1.getId());

            //then
            assertThat(memberOfTheCommunity).isEqualTo(member1);
        }
    }

    @Nested
    @DisplayName("커뮤니티의 내부를 열람 가능한 멤버를 조회할때")
    class GetViewableMemberTest {

        @Test
        @DisplayName("공개 커뮤니티에 가입된 멤버의 경우 해당 멤버를 가져온다.")
        void publicCommunityJoinedMemberSuccess() {
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member2));

            Member viewableMember = memberQuery.getViewableMember(user.getId(), community2);

            assertThat(viewableMember.getId()).isEqualTo(member2.getId());
            assertThat(viewableMember.isNullMember()).isFalse();
        }

        @Test
        @DisplayName("공개 커뮤니티에 가입하지 않은 멤버의 경우 NullMember를 가져온다.")
        void publicCommunityNotJoinedMemberSuccess() {
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.empty());

            Member viewableMember = memberQuery.getViewableMember(user.getId(), community1);

            assertThat(viewableMember).isEqualTo(new NullMember());
            assertThat(viewableMember.isNullMember()).isTrue();
        }

        @Test
        @DisplayName("비공개 커뮤니티에 가입된 멤버의 경우 해당 멤버를 가져온다.")
        void privateCommunityJoinedMemberSuccess() {
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member2));

            Member viewableMember = memberQuery.getViewableMember(user.getId(), community2);

            assertThat(viewableMember.getId()).isEqualTo(member2.getId());
            assertThat(viewableMember.isNullMember()).isFalse();
        }

        @Test
        @DisplayName("비공개 커뮤니티에 가입되어있지 않는 멤버일 경우 NotViewableMemberException이 발생한다.")
        void privateCommunityNotJoinedMemberFail() {
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> memberQuery.getViewableMember(user.getId(), community2))
                    .isInstanceOf(NotViewableMemberException.class);
        }
    }
}