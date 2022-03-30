package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberCoreServiceTest {

    @InjectMocks
    MemberCoreService memberCoreService;

    @Mock
    MemberRepository memberRepository;

    @Mock
    MemberQueryService memberQueryService;

    @Mock
    MemberValidationService memberValidationService;

    @Mock
    CommunityQueryService communityQueryService;

    @Mock
    UserQueryService userQueryService;

    @Test
    void 멤버_가입_성공() {
        //given
        User user = User.builder()
                .id(1L)
                .build();
        given(userQueryService.getUser(anyLong()))
                .willReturn(user);

        Community community = Community.builder()
                .id(2L)
                .build();
        given(communityQueryService.getCommunity(anyLong()))
                .willReturn(community);

        //when
        Member member = memberCoreService.joinMember(user.getId(), community.getId(), MemberType.MANAGER);

        //then
        assertThat(member.getCommunity().getId()).isEqualTo(community.getId());
        assertThat(member.getUser().getId()).isEqualTo(user.getId());
        assertThat(member.getCommunity().getMemberCount()).isEqualTo(1);
    }

    @Test
    void 이미_차단한_멤버() {
        Member member = Member.builder()
                .id(1L)
                .bannedAt(LocalDateTime.now())
                .build();


        given(memberQueryService.getMember(anyLong()))
                .willReturn(member);

        assertThatThrownBy(() -> {
            memberCoreService.banMember(member.getId());
        })
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("이미 차단된 멤버입니다.");

    }

    @Test
    void 멤버_차단안된_멤버를_차단시도() {
        //given
        Member member = Member.builder()
                .id(1L)
                .build();

        given(memberQueryService.getMember(anyLong()))
                .willReturn(member);

        //then
        assertThatThrownBy(() -> {
            //when
            memberCoreService.releaseMember(member.getId());
        }).isInstanceOf(InvalidValueException.class);
    }
}