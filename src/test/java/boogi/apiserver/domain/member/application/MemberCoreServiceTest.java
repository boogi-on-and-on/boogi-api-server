package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.community.community.application.CommunityCoreService;
import boogi.apiserver.domain.community.community.application.CommunityValidationService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.AlreadyJoinedMemberException;
import boogi.apiserver.domain.user.application.UserCoreService;
import boogi.apiserver.domain.user.application.UserValidationService;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberCoreServiceTest {

    @InjectMocks
    MemberCoreService memberCoreService;

    @Mock
    MemberRepository memberRepository;

    @Mock
    UserValidationService userValidationService;

    @Mock
    CommunityValidationService communityValidationService;

    @Mock
    MemberValidationService memberValidationService;

    @Test
    void 멤버_가입_성공() {
        //given
        User user = User.builder()
                .id(1L)
                .build();
        given(userValidationService.getUser(anyLong()))
                .willReturn(user);

        Community community = Community.builder()
                .id(2L)
                .build();
        given(communityValidationService.getCommunity(anyLong()))
                .willReturn(community);

        //when
        Member member = memberCoreService.joinMember(user.getId(), community.getId(), MemberType.MANAGER);

        //then
        assertThat(member.getCommunity().getId()).isEqualTo(community.getId());
        assertThat(member.getUser().getId()).isEqualTo(user.getId());
        assertThat(member.getCommunity().getMemberCount()).isEqualTo(1);
    }
}