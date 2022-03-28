package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.community.application.CommunityHashtagCoreService;
import boogi.apiserver.domain.hashtag.community.dao.CommunityHashtagRepository;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import boogi.apiserver.domain.member.application.MemberCoreService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.global.error.exception.InvalidValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CommunityCoreServiceTest {

    @Mock
    CommunityRepository communityRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    CommunityHashtagRepository communityHashtagRepository;

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

        then(community).should(times(1)).shutdown();
    }

    @Test
    void 커뮤니티_업데이트할_때_새로운테그와_이전테그_없는경우() {
        //given
        Community community = mock(Community.class);
        given(communityQueryService.getCommunity(anyLong())).willReturn(community);

        //when
        communityCoreService.update(1L, "123", null);

        //then
        then(communityHashtagRepository).should(times(0)).deleteAllInBatch();
    }

    @Test
    void 커뮤니티_업데이트할_때_테그가_같은경우() {
        //given
        Community community = Community.builder()
                .id(1L)
                .hashtags(List.of(
                        CommunityHashtag.builder().tag("테그2").build(),
                        CommunityHashtag.builder().tag("테그1").build()
                )).build();

        given(communityQueryService.getCommunity(anyLong())).willReturn(community);

        List<String> newTags = new ArrayList<>();
        newTags.add("테그1");
        newTags.add("테그2");

        //when
        communityCoreService.update(1L, "1231232123", newTags);

        //then
        then(communityHashtagRepository).should(times(0)).deleteAllInBatch();
        then(communityHashtagRepository).should(times(0)).saveAll(any());
    }

    @Test
    void 커뮤니티_업데이트할_때_이전테그_삭제() {
        //given
        Community community = Community.builder()
                .id(1L)
                .hashtags(List.of(
                        CommunityHashtag.builder().tag("테그2").build(),
                        CommunityHashtag.builder().tag("테그1").build()
                )).build();

        given(communityQueryService.getCommunity(any())).willReturn(community);

        //when
        communityCoreService.update(1L, "2143242343", null);

        //then
        then(communityHashtagRepository).should().deleteAllInBatch(any());
        then(communityHashtagRepository).should(times(0)).saveAll(any());
    }

    @Test
    void 커뮤니티_업데이트할_때_이전_테그_삭제_후_새로운_테그_업데이트() {
        //given
        List<CommunityHashtag> prevHashtags = new ArrayList<>();
        prevHashtags.add(CommunityHashtag.builder().id(1L).tag("테그2").build());
        prevHashtags.add(CommunityHashtag.builder().id(2L).tag("테그1").build());

        Community community = Community.builder()
                .id(1L)
                .hashtags(prevHashtags)
                .build();

        given(communityQueryService.getCommunity(any())).willReturn(community);

        List<String> newTags = new ArrayList<>();
        newTags.add("테그2");
        newTags.add("BB테그1");

        //when
        communityCoreService.update(1L, "2143242343", newTags);

        //then
        then(communityHashtagRepository).should().deleteAllInBatch(prevHashtags);

        then(communityHashtagRepository).should().saveAll(any());
    }
}