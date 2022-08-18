package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.community.dao.CommunityHashtagRepository;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.global.error.exception.InvalidValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CommunityCoreServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    CommunityHashtagRepository communityHashtagRepository;

    @Mock
    CommunityQueryService communityQueryService;

    @InjectMocks
    CommunityCoreService communityCoreService;

    @Nested
    @DisplayName("커뮤니티 폐쇄 테스트")
    class BlockCommunity {

        @Test
        @DisplayName("폐쇄 실패")
        void failBlockCommunity() {
            //given
            Community community = Community.builder()
                    .id(1L)
                    .build();

            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            Member member = Member.builder().build();
            given(memberRepository.findAnyMemberExceptManager(any()))
                    .willReturn(Optional.of(member));

            //then
            assertThatThrownBy(() -> {
                //when
                communityCoreService.shutdown(community.getId());
            }).isInstanceOf(InvalidValueException.class);
        }

        @Test
        @DisplayName("생성 성공")
        void success() {
            Community community = mock(Community.class);

            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            Member member = Member.builder().build();
            given(memberRepository.findAnyMemberExceptManager(any()))
                    .willReturn(Optional.empty());

            communityCoreService.shutdown(community.getId());

            then(community).should(times(1)).shutdown();
        }
    }

    @Nested
    @DisplayName("커뮤니티 업데이트 테스트")
    class CommunityUpdateTest {

        @Test
        @DisplayName("새로운 테그와 이전 테그가 없는 경우")
        void thereIsNoTagOrPrevTag() {
            //given
            Community community = mock(Community.class);
            given(communityQueryService.getCommunity(anyLong())).willReturn(community);

            //when
            communityCoreService.update(1L, "123", null);

            //then
            then(communityHashtagRepository).should(times(0)).deleteAllInBatch();
        }

        @Test
        @DisplayName("이전 테그와 새로운 테그가 같은 경우")
        void sameCommunityTag() {
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
        @DisplayName("테그를 삭제하는 경우")
        void deleteAllPrevTag() {
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
        @DisplayName("이전 테그 삭제 후, 새로운 테그 업데이트")
        void deleteAllPrevTagAndInsertNewTag() {
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
}