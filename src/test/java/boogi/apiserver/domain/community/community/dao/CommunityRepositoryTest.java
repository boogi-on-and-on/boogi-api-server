package boogi.apiserver.domain.community.community.dao;


import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.dto.enums.CommunityListingOrder;
import boogi.apiserver.domain.community.community.dto.request.CommunityQueryRequest;
import boogi.apiserver.domain.community.community.dto.dto.SearchCommunityDto;
import boogi.apiserver.domain.community.community.exception.CommunityNotFoundException;
import boogi.apiserver.domain.hashtag.community.dao.CommunityHashtagRepository;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import boogi.apiserver.utils.PersistenceUtil;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CustomDataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommunityRepositoryTest {

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private CommunityHashtagRepository communityHashtagRepository;

    @Autowired
    EntityManager em;

    PersistenceUtil persistenceUtil;
    @Autowired
    private CommentRepository commentRepository;

    @BeforeAll
    void init() {
        persistenceUtil = new PersistenceUtil(em);
    }

    @Nested
    @DisplayName("커뮤니티 검색 테스트")
    class CommunitySearchTest {
        @Test
        @DisplayName("커뮤니티 검색 키워드가 있을 경우")
        void ThereIsSearchKeyword() {

            //given
            final Community c1 = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(c1, "communityName", "커뮤니티1");
            ReflectionTestUtils.setField(c1, "isPrivate", true);
            ReflectionTestUtils.setField(c1, "category", CommunityCategory.ACADEMIC);
            ReflectionTestUtils.setField(c1, "memberCount", 12);
            ReflectionTestUtils.setField(c1, "description", "소개");
            ReflectionTestUtils.setField(c1, "createdAt", LocalDateTime.now());

            final Community c2 = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(c2, "communityName", "커뮤니티2");
            ReflectionTestUtils.setField(c2, "isPrivate", true);
            ReflectionTestUtils.setField(c2, "category", CommunityCategory.ACADEMIC);
            ReflectionTestUtils.setField(c2, "memberCount", 23);
            ReflectionTestUtils.setField(c2, "description", "안녕");
            ReflectionTestUtils.setField(c2, "createdAt", LocalDateTime.now().minusDays(2));


            communityRepository.saveAll(List.of(c1, c2));

            CommunityHashtag c1_t1 = CommunityHashtag.of("안녕", c1);
            CommunityHashtag c1_t2 = CommunityHashtag.of("ㅎㅎ", c1);
            CommunityHashtag c2_t1 = CommunityHashtag.of("zz", c2);
            communityHashtagRepository.saveAll(List.of(c1_t1, c1_t2, c2_t1));


            CommunityQueryRequest request = new CommunityQueryRequest(CommunityCategory.ACADEMIC, true, CommunityListingOrder.NEWER, "안녕");

            em.flush();
            em.clear();

            //when
            Slice<SearchCommunityDto> page = communityRepository.getSearchedCommunities(PageRequest.of(0, 2), request);

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
            final Community c1 = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(c1, "communityName", "커뮤니티1");
            ReflectionTestUtils.setField(c1, "isPrivate", true);
            ReflectionTestUtils.setField(c1, "category", CommunityCategory.ACADEMIC);
            ReflectionTestUtils.setField(c1, "memberCount", 12);
            ReflectionTestUtils.setField(c1, "description", "소개");
            ReflectionTestUtils.setField(c1, "createdAt", LocalDateTime.now());

            //given
            final Community c2 = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(c2, "communityName", "커뮤니티2");
            ReflectionTestUtils.setField(c2, "isPrivate", true);
            ReflectionTestUtils.setField(c2, "category", CommunityCategory.ACADEMIC);
            ReflectionTestUtils.setField(c2, "memberCount", 23);
            ReflectionTestUtils.setField(c2, "description", "안녕");
            ReflectionTestUtils.setField(c2, "createdAt", LocalDateTime.now().minusDays(2));

            communityRepository.saveAll(List.of(c1, c2));

            CommunityHashtag c1_t1 = CommunityHashtag.of("안녕", c1);
            CommunityHashtag c1_t2 = CommunityHashtag.of("ㅎㅎ", c1);
            CommunityHashtag c2_t1 = CommunityHashtag.of("zz", c2);
            communityHashtagRepository.saveAll(List.of(c1_t1, c1_t2, c2_t1));

            CommunityQueryRequest request = new CommunityQueryRequest(CommunityCategory.ACADEMIC, true, CommunityListingOrder.NEWER, null);

            em.flush();
            em.clear();

            //when

            Slice<SearchCommunityDto> slice = communityRepository.getSearchedCommunities(PageRequest.of(0, 2), request);

            //then
            List<SearchCommunityDto> dtos = slice.getContent();
            assertThat(dtos.size()).isEqualTo(2);

            SearchCommunityDto first = dtos.get(0);
            assertThat(first.getId()).isEqualTo(c1.getId());

            assertThat(first.getCategory()).isEqualTo(CommunityCategory.ACADEMIC.toString());
            assertThat(first.getDescription()).isEqualTo("소개");
            assertThat(first.getMemberCount()).isEqualTo(12);
            assertThat(first.getName()).isEqualTo("커뮤니티1");
        }
    }

    @Nested
    @DisplayName("findByCommunityId 디폴트 메서드 테스트")
    class findByCommunityId {
        @DisplayName("성공")
        @Test
        void success() {
            final Community community = TestEmptyEntityGenerator.Community();
            communityRepository.save(community);

            persistenceUtil.cleanPersistenceContext();

            final Community findCommunity = communityRepository.findByCommunityId(community.getId());
            assertThat(findCommunity.getId()).isEqualTo(community.getId());
        }

        @DisplayName("throw CommunityNotFoundException")
        @Test
        void throwException() {
            assertThatThrownBy(() -> {
                communityRepository.findByCommunityId(1L);
            }).isInstanceOf(CommunityNotFoundException.class);
        }
    }
}