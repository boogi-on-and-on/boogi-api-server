package boogi.apiserver.domain.hashtag.community.application;

import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.community.dao.CommunityHashtagRepository;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommunityHashtagServiceTest {

    @Mock
    CommunityQueryService communityQueryService;

    @Mock
    CommunityHashtagRepository communityHashtagRepository;

    @InjectMocks
    CommunityHashtagCoreService communityHashtagCoreService;

    @Nested
    @DisplayName("커뮤니티 해시테그 저장")
    class saveCommunityHashtag {
        @Test
        @DisplayName("저장 성공")
        void success() {
            //given
            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            List<String> tags = List.of("테그1", "테그2");

            final CommunityHashtag hashtag1 = TestEmptyEntityGenerator.CommunityHashtag();
            ReflectionTestUtils.setField(hashtag1, "tag", "테그1");
            ReflectionTestUtils.setField(hashtag1, "community", community);

            final CommunityHashtag hashtag2 = TestEmptyEntityGenerator.CommunityHashtag();
            ReflectionTestUtils.setField(hashtag2, "tag", "테그2");
            ReflectionTestUtils.setField(hashtag2, "community", community);

            given(communityHashtagRepository.saveAll(any()))
                    .willReturn(List.of(hashtag1, hashtag2));

            //when
            List<CommunityHashtag> communityHashtags = communityHashtagCoreService.addTags(community.getId(), tags);

            //then
            assertThat(communityHashtags.size()).isEqualTo(2);
            assertThat(communityHashtags.get(0).getTag()).isEqualTo("테그1");
            assertThat(communityHashtags.get(1).getTag()).isEqualTo("테그2");
        }

        @Test
        @DisplayName("tag is null")
        void tagIsNullable() {
            List<CommunityHashtag> communityHashtags = communityHashtagCoreService.addTags(1L, null);
            assertThat(communityHashtags).isNull();
        }

        @Test
        @DisplayName("tag is empty")
        void tagIsEmpty() {
            List<CommunityHashtag> communityHashtags = communityHashtagCoreService.addTags(1L, new ArrayList<>());
            assertThat(communityHashtags).isNull();
        }
    }
}