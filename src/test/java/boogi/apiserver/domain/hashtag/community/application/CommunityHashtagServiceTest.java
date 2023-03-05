package boogi.apiserver.domain.hashtag.community.application;

import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.community.dao.CommunityHashtagRepository;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommunityHashtagServiceTest {

    @Mock
    CommunityRepository communityRepository;

    @Mock
    CommunityHashtagRepository communityHashtagRepository;

    @InjectMocks
    CommunityHashtagService communityHashtagService;

    @Nested
    @DisplayName("커뮤니티 해시테그 저장")
    class saveCommunityHashtag {
        @Test
        @DisplayName("저장 성공")
        void success() {
            //given
            final Community community = TestCommunity.builder()
                    .id(1L)
                    .build();

            given(communityRepository.findByCommunityId(anyLong()))
                    .willReturn(community);

            final CommunityHashtag hashtag1 = CommunityHashtag.builder()
                    .tag("테그A")
                    .community(community)
                    .build();

            final CommunityHashtag hashtag2 = CommunityHashtag.builder()
                    .tag("테그B")
                    .community(community)
                    .build();

            given(communityHashtagRepository.saveAll(any()))
                    .willReturn(List.of(hashtag1, hashtag2));

            //when
            List<CommunityHashtag> communityHashtags = communityHashtagService.addTags(community.getId(), List.of("테그A", "테그B"));

            //then
            assertThat(communityHashtags.size()).isEqualTo(2);
            assertThat(communityHashtags.get(0).getTag()).isEqualTo("테그A");
            assertThat(communityHashtags.get(1).getTag()).isEqualTo("테그B");
        }

        @Test
        @DisplayName("tag is null")
        void tagIsNullable() {
            List<CommunityHashtag> communityHashtags = communityHashtagService.addTags(1L, null);
            assertThat(communityHashtags).isNull();
        }

        @Test
        @DisplayName("tag is empty")
        void tagIsEmpty() {
            List<CommunityHashtag> communityHashtags = communityHashtagService.addTags(1L, new ArrayList<>());
            assertThat(communityHashtags).isNull();
        }
    }
}