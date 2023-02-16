package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.community.dao.CommunityHashtagRepository;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.global.error.exception.InvalidValueException;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    CommunityHashtagRepository communityHashtagRepository;

    @Mock
    CommunityQueryService communityQueryService;

    @InjectMocks
    CommunityService communityService;

    @Nested
    @DisplayName("커뮤니티 폐쇄 테스트")
    class BlockCommunity {

        @Test
        @DisplayName("폐쇄 실패")
        void failBlockCommunity() {
            //given
            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            final Member member = TestEmptyEntityGenerator.Member();

            given(memberRepository.findAnyMemberExceptManager(any()))
                    .willReturn(Optional.of(member));

            //then
            assertThatThrownBy(() -> {
                //when
                communityService.shutdown(community.getId());
            }).isInstanceOf(InvalidValueException.class);
        }

        @Test
        @DisplayName("생성 성공")
        void success() {
            Community community = mock(Community.class);

            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            final Member member = TestEmptyEntityGenerator.Member();

            given(memberRepository.findAnyMemberExceptManager(any()))
                    .willReturn(Optional.empty());

            communityService.shutdown(community.getId());

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
            communityService.update(1L, "123", null);

            //then
            then(communityHashtagRepository).should(times(0)).deleteAllInBatch();
        }

        @Test
        @DisplayName("이전 테그와 새로운 테그가 같은 경우")
        void sameCommunityTag() {
            //given

            final CommunityHashtag hashtag1 = TestEmptyEntityGenerator.CommunityHashtag();
            ReflectionTestUtils.setField(hashtag1, "tag", "테그1");

            final CommunityHashtag hashtag2 = TestEmptyEntityGenerator.CommunityHashtag();
            ReflectionTestUtils.setField(hashtag2, "tag", "테그2");

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);
            ReflectionTestUtils.setField(community, "hashtags", List.of(hashtag2, hashtag1));

            given(communityQueryService.getCommunity(anyLong())).willReturn(community);

            List<String> newTags = new ArrayList<>();
            newTags.add("테그1");
            newTags.add("테그2");

            //when
            communityService.update(1L, "1231232123", newTags);

            //then
            then(communityHashtagRepository).should(times(0)).deleteAllInBatch();
            then(communityHashtagRepository).should(times(0)).saveAll(any());
        }

        @Test
        @DisplayName("테그를 삭제하는 경우")
        void deleteAllPrevTag() {
            //given
            final CommunityHashtag hashtag1 = TestEmptyEntityGenerator.CommunityHashtag();
            ReflectionTestUtils.setField(hashtag1, "tag", "테그1");

            final CommunityHashtag hashtag2 = TestEmptyEntityGenerator.CommunityHashtag();
            ReflectionTestUtils.setField(hashtag2, "tag", "테그2");

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);
            ReflectionTestUtils.setField(community, "hashtags", List.of(hashtag2, hashtag1));

            given(communityQueryService.getCommunity(any())).willReturn(community);

            //when
            communityService.update(1L, "2143242343", null);

            //then
            then(communityHashtagRepository).should().deleteAllInBatch(any());
            then(communityHashtagRepository).should(times(0)).saveAll(any());
        }

        @Test
        @DisplayName("이전 테그 삭제 후, 새로운 테그 업데이트")
        void deleteAllPrevTagAndInsertNewTag() {
            //given
            List<CommunityHashtag> prevHashtags = new ArrayList<>();

            final CommunityHashtag hashtag1 = TestEmptyEntityGenerator.CommunityHashtag();
            ReflectionTestUtils.setField(hashtag1, "id", 1L);
            ReflectionTestUtils.setField(hashtag1, "tag", "테그1");

            final CommunityHashtag hashtag2 = TestEmptyEntityGenerator.CommunityHashtag();
            ReflectionTestUtils.setField(hashtag2, "id", 2L);
            ReflectionTestUtils.setField(hashtag2, "tag", "테그2");

            prevHashtags.add(hashtag2);
            prevHashtags.add(hashtag1);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);
            ReflectionTestUtils.setField(community, "hashtags", prevHashtags);

            given(communityQueryService.getCommunity(any())).willReturn(community);

            List<String> newTags = new ArrayList<>();
            newTags.add("테그2");
            newTags.add("BB테그1");

            //when
            communityService.update(1L, "2143242343", newTags);

            //then
            then(communityHashtagRepository).should().deleteAllInBatch(prevHashtags);

            then(communityHashtagRepository).should().saveAll(any());
        }
    }
}