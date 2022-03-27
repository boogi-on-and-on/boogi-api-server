package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.community.application.CommunityHashtagCoreService;
import boogi.apiserver.domain.member.application.MemberCoreService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.global.error.exception.InvalidValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CommunityCoreServiceTest {

    @Mock
    CommunityRepository communityRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    MemberCoreService memberCoreService;

    @Mock
    CommunityHashtagCoreService communityHashtagCoreService;

    @Mock
    CommunityValidationService communityValidationService;

    @Mock
    CommunityQueryService communityQueryService;

    @InjectMocks
    CommunityCoreService communityCoreService;


    //    @Test
    void 커뮤니티생성_성공() {
    }

    @Test
    void 커뮤니티_폐쇄_실패() {
        //given
        Community community = Community.builder()
                .id(1L)
                .build();

        given(communityQueryService.getCommunity(anyLong()))
                .willReturn(community);

        Member member = Member.builder().build();
        given(memberRepository.findAnyMemberExceptManager(any()))
                .willReturn(member);

        //then
        assertThatThrownBy(() -> {
            //when
            communityCoreService.shutdown(community.getId());
        }).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void 커뮤니티_폐쇄_성공() {
        Community community = mock(Community.class);

        given(communityQueryService.getCommunity(anyLong()))
                .willReturn(community);

        Member member = Member.builder().build();
        given(memberRepository.findAnyMemberExceptManager(any()))
                .willReturn(null);

        communityCoreService.shutdown(community.getId());

        BDDMockito.then(community).should(times(1)).shutdown();
    }
}