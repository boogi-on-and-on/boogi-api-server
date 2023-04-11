package boogi.apiserver.domain.community.community.dao;


import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.dto.dto.SearchCommunityDto;
import boogi.apiserver.domain.community.community.dto.enums.CommunityListingOrder;
import boogi.apiserver.domain.community.community.dto.request.CommunityQueryRequest;
import boogi.apiserver.domain.community.community.exception.CommunityNotFoundException;
import boogi.apiserver.domain.hashtag.community.dao.CommunityHashtagRepository;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import boogi.apiserver.utils.RepositoryTest;
import boogi.apiserver.utils.TestTimeReflection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommunityRepositoryTest extends RepositoryTest {

    @Autowired
    CommunityRepository communityRepository;

    @Autowired
    CommunityHashtagRepository communityHashtagRepository;

    @Nested
    @DisplayName("ID로 커뮤니티 조회")
    class findCommunityById {
        @DisplayName("성공")
        @Test
        void success() {
            final Community community = TestCommunity.builder().build();
            communityRepository.save(community);

            cleanPersistenceContext();

            final Community findCommunity = communityRepository.findCommunityById(community.getId());
            assertThat(findCommunity.getId()).isEqualTo(community.getId());
        }

        @DisplayName("throw CommunityNotFoundException")
        @Test
        void throwException() {
            assertThatThrownBy(() -> {
                communityRepository.findCommunityById(1L);
            }).isInstanceOf(CommunityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("커뮤니티 검색 테스트")
    class CommunitySearchTest {

        @Test
        @DisplayName("커뮤니티 검색 키워드가 있을 경우")
        void ThereIsSearchKeyword() {
            //given
            final Community c1 = TestCommunity.builder()
                    .communityName("커뮤니티A")
                    .isPrivate(true)
                    .category(CommunityCategory.ACADEMIC)
                    .memberCount(12)
                    .description("커뮤니티A의 소개란 입니다.")
                    .build();
            TestTimeReflection.setCreatedAt(c1, LocalDateTime.now());

            final Community c2 = TestCommunity.builder()
                    .communityName("커뮤니티B")
                    .isPrivate(true)
                    .category(CommunityCategory.ACADEMIC)
                    .memberCount(23)
                    .description("커뮤니티B의 소개란 입니다.")
                    .build();
            TestTimeReflection.setCreatedAt(c2, LocalDateTime.now().minusDays(2));

            communityRepository.saveAll(List.of(c1, c2));

            final CommunityHashtag c1_t1 = CommunityHashtag.of("안녕", c1);
            final CommunityHashtag c1_t2 = CommunityHashtag.of("ㅎㅎ", c1);
            final CommunityHashtag c2_t1 = CommunityHashtag.of("zz", c2);
            communityHashtagRepository.saveAll(List.of(c1_t1, c1_t2, c2_t1));

            final CommunityQueryRequest request =
                    new CommunityQueryRequest(CommunityCategory.ACADEMIC, true, CommunityListingOrder.NEWER, "안녕");

            cleanPersistenceContext();

            //when
            final Slice<SearchCommunityDto> page =
                    communityRepository.getSearchedCommunities(PageRequest.of(0, 2), request);

            //then
            final List<SearchCommunityDto> dtos = page.getContent();
            assertThat(dtos).hasSize(1);

            final SearchCommunityDto first = dtos.get(0);
            assertThat(first.getId()).isEqualTo(c1.getId());

            assertThat(first.getHashtags()).containsExactlyInAnyOrder("안녕", "ㅎㅎ");
            assertThat(first.getCategory()).isEqualTo(CommunityCategory.ACADEMIC.toString());
            assertThat(first.getDescription()).isEqualTo("커뮤니티A의 소개란 입니다.");
            assertThat(first.getMemberCount()).isEqualTo(12);
            assertThat(first.getName()).isEqualTo("커뮤니티A");
        }

        @Test
        @DisplayName("커뮤니티 검색 키워드가 없는 경우")
        void ThereIsNoSearchKeyword() {
            final Community c1 = TestCommunity.builder()
                    .communityName("커뮤니티A")
                    .isPrivate(true)
                    .category(CommunityCategory.ACADEMIC)
                    .memberCount(12)
                    .description("커뮤니티A의 소개란 입니다.")
                    .build();
            TestTimeReflection.setCreatedAt(c1, LocalDateTime.now());

            //given
            final Community c2 = TestCommunity.builder()
                    .communityName("커뮤니티B")
                    .isPrivate(true)
                    .category(CommunityCategory.ACADEMIC)
                    .memberCount(23)
                    .description("커뮤니티B의 소개란 입니다.")
                    .build();
            TestTimeReflection.setCreatedAt(c2, LocalDateTime.now().minusDays(2));

            communityRepository.saveAll(List.of(c1, c2));

            final CommunityHashtag c1_t1 = CommunityHashtag.of("안녕", c1);
            final CommunityHashtag c1_t2 = CommunityHashtag.of("ㅎㅎ", c1);
            final CommunityHashtag c2_t1 = CommunityHashtag.of("zz", c2);
            communityHashtagRepository.saveAll(List.of(c1_t1, c1_t2, c2_t1));

            CommunityQueryRequest request =
                    new CommunityQueryRequest(CommunityCategory.ACADEMIC, true, CommunityListingOrder.NEWER, null);

            cleanPersistenceContext();

            //when
            final Slice<SearchCommunityDto> slice =
                    communityRepository.getSearchedCommunities(PageRequest.of(0, 2), request);

            //then
            final List<SearchCommunityDto> dtos = slice.getContent();
            assertThat(dtos).hasSize(2);

            final SearchCommunityDto first = dtos.get(0);
            assertThat(first.getId()).isEqualTo(c1.getId());

            assertThat(first.getCategory()).isEqualTo(CommunityCategory.ACADEMIC.toString());
            assertThat(first.getDescription()).isEqualTo("커뮤니티A의 소개란 입니다.");
            assertThat(first.getMemberCount()).isEqualTo(12);
            assertThat(first.getName()).isEqualTo("커뮤니티A");
        }

        //todo: 검색 테스트 케이스 추가
    }
}