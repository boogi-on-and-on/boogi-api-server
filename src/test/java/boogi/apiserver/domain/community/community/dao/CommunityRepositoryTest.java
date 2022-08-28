package boogi.apiserver.domain.community.community.dao;


import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.dto.enums.CommunityListingOrder;
import boogi.apiserver.domain.community.community.dto.request.CommunityQueryRequest;
import boogi.apiserver.domain.community.community.dto.response.SearchCommunityDto;
import boogi.apiserver.domain.hashtag.community.dao.CommunityHashtagRepository;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CustomDataJpaTest
class CommunityRepositoryTest {

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private CommunityHashtagRepository communityHashtagRepository;

    @Autowired
    EntityManager em;

    @Nested
    @DisplayName("커뮤니티 검색 테스트")
    class CommunitySearchTest {
        @Test
        @DisplayName("커뮤니티 검색 키워드가 있을 경우")
        void ThereIsSearchKeyword() {

            //given
            Community c1 = Community.builder()
                    .communityName("커뮤니티1")
                    .isPrivate(true)
                    .category(CommunityCategory.ACADEMIC)
                    .memberCount(12)
                    .hashtags(new ArrayList<>())
                    .description("소개")
                    .build();
            c1.setCreatedAt(LocalDateTime.now());

            Community c2 = Community.builder()
                    .communityName("커뮤니티2")
                    .isPrivate(true)
                    .category(CommunityCategory.ACADEMIC)
                    .memberCount(23)
                    .hashtags(new ArrayList<>())
                    .description("안녕")
                    .build();
            c2.setCreatedAt(LocalDateTime.now().minusDays(2));

            communityRepository.saveAll(List.of(c1, c2));

            CommunityHashtag c1_t1 = CommunityHashtag.of("안녕", c1);
            CommunityHashtag c1_t2 = CommunityHashtag.of("ㅎㅎ", c1);
            CommunityHashtag c2_t1 = CommunityHashtag.of("zz", c2);
            communityHashtagRepository.saveAll(List.of(c1_t1, c1_t2, c2_t1));


            CommunityQueryRequest request = CommunityQueryRequest.builder()
                    .isPrivate(true)
                    .category(CommunityCategory.ACADEMIC)
                    .order(CommunityListingOrder.NEWER)
                    .keyword("안녕")
                    .build();

            em.flush();
            em.clear();

            //when
            Page<SearchCommunityDto> page = communityRepository.getSearchedCommunities(PageRequest.of(0, 2), request);

            //then
            List<SearchCommunityDto> dtos = page.getContent();
            assertThat(dtos.size()).isEqualTo(1);

            SearchCommunityDto first = dtos.get(0);
            assertThat(first.getId()).isEqualTo(c1.getId());

            assertThat(first.getHashtags()).containsExactlyInAnyOrderElementsOf(List.of("안녕", "ㅎㅎ"));
            assertThat(first.getCategory()).isEqualTo(CommunityCategory.ACADEMIC.toString());
            assertThat(first.getDescription()).isEqualTo("소개");
            assertThat(first.getMemberCount()).isEqualTo(12);
            assertThat(first.getName()).isEqualTo("커뮤니티1");
        }

        @Test
        @DisplayName("커뮤니티 검색 키워드가 없는 경우")
        void ThereIsNoSearchKeyword() {
            //given
            Community c1 = Community.builder()
                    .communityName("커뮤니티1")
                    .isPrivate(true)
                    .category(CommunityCategory.ACADEMIC)
                    .memberCount(12)
                    .hashtags(new ArrayList<>())
                    .description("소개")
                    .build();
            c1.setCreatedAt(LocalDateTime.now());

            Community c2 = Community.builder()
                    .communityName("커뮤니티2")
                    .isPrivate(true)
                    .category(CommunityCategory.ACADEMIC)
                    .memberCount(23)
                    .hashtags(new ArrayList<>())
                    .description("안녕")
                    .build();
            c2.setCreatedAt(LocalDateTime.now().minusDays(2));

            communityRepository.saveAll(List.of(c1, c2));

            CommunityHashtag c1_t1 = CommunityHashtag.of("안녕", c1);
            CommunityHashtag c1_t2 = CommunityHashtag.of("ㅎㅎ", c1);
            CommunityHashtag c2_t1 = CommunityHashtag.of("zz", c2);
            communityHashtagRepository.saveAll(List.of(c1_t1, c1_t2, c2_t1));

            CommunityQueryRequest request = CommunityQueryRequest.builder()
                    .isPrivate(true)
                    .category(CommunityCategory.ACADEMIC)
                    .order(CommunityListingOrder.NEWER)
                    .build();

            em.flush();
            em.clear();

            //when

            Page<SearchCommunityDto> page = communityRepository.getSearchedCommunities(PageRequest.of(0, 2), request);

            //then
            List<SearchCommunityDto> dtos = page.getContent();
            assertThat(dtos.size()).isEqualTo(2);

            SearchCommunityDto first = dtos.get(0);
            assertThat(first.getId()).isEqualTo(c1.getId());

            assertThat(first.getCategory()).isEqualTo(CommunityCategory.ACADEMIC.toString());
            assertThat(first.getDescription()).isEqualTo("소개");
            assertThat(first.getMemberCount()).isEqualTo(12);
            assertThat(first.getName()).isEqualTo("커뮤니티1");
        }
    }
}