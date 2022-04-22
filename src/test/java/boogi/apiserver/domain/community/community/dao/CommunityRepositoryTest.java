package boogi.apiserver.domain.community.community.dao;


import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.dto.CommunityQueryRequest;
import boogi.apiserver.domain.community.community.dto.SearchCommunityDto;
import boogi.apiserver.domain.community.community.dto.request_enum.CommunityListingOrder;
import boogi.apiserver.domain.hashtag.community.dao.CommunityHashtagRepository;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class CommunityRepositoryTest {

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private CommunityHashtagRepository communityHashtagRepository;

    @Autowired
    EntityManager em;

    @Test
    void 커뮤니티_검색_키워드_있을때() {

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
    void 커뮤니티_검색_키워드_없을때() {
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