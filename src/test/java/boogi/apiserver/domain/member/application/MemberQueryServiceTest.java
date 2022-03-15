package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.UserJoinedCommunity;
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
class MemberQueryServiceTest {

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    MemberQueryService memberQueryService;
    private Object Assertions;

    @Test
    void 특정유저가_가입한_멤버_목록_조회() {

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
        return dtos.stream().filter(d -> d.getId().equals(id.toString())).findFirst().get();
    }
}