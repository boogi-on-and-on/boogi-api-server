package boogi.apiserver.domain.community.community.repository;


import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.dto.dto.SearchCommunityDto;
import boogi.apiserver.domain.community.community.dto.enums.CommunityListingOrder;
import boogi.apiserver.domain.community.community.dto.request.CommunityQueryRequest;
import boogi.apiserver.domain.community.community.exception.CommunityNotFoundException;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import boogi.apiserver.domain.hashtag.community.repository.CommunityHashtagRepository;
import boogi.apiserver.utils.RepositoryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.stream.Collectors;

import static boogi.apiserver.utils.fixture.CommunityFixture.BASEBALL;
import static boogi.apiserver.utils.fixture.CommunityFixture.POCS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

class CommunityRepositoryTest extends RepositoryTest {

    @Autowired
    CommunityRepository communityRepository;

    @Autowired
    CommunityHashtagRepository communityHashtagRepository;

    private Community community1;
    private Community community2;

    @BeforeEach
    public void init() {
        this.community1 = BASEBALL.toCommunity();
        this.community2 = POCS.toCommunity();
    }

    @Nested
    @DisplayName("ID로 커뮤니티 조회")
    class findCommunityById {
        @DisplayName("성공")
        @Test
        void success() {
            communityRepository.save(community1);

            cleanPersistenceContext();

            final Community findCommunity = communityRepository.findCommunityById(community1.getId());
            assertThat(findCommunity.getId()).isEqualTo(community1.getId());
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
            communityRepository.saveAll(List.of(community1, community2));

            final CommunityHashtag c1_t1 = CommunityHashtag.of("안녕", community1);
            final CommunityHashtag c1_t2 = CommunityHashtag.of("ㅎㅎ", community1);
            final CommunityHashtag c2_t1 = CommunityHashtag.of("zz", community2);
            communityHashtagRepository.saveAll(List.of(c1_t1, c1_t2, c2_t1));

            final CommunityQueryRequest request =
                    new CommunityQueryRequest(CommunityCategory.ACADEMIC, false, CommunityListingOrder.NEWER, "안녕");

            cleanPersistenceContext();

            //when
            final Slice<SearchCommunityDto> page =
                    communityRepository.getSearchedCommunities(PageRequest.of(0, 2), request);

            //then
            final List<SearchCommunityDto> dtos = page.getContent();
            assertThat(dtos).hasSize(1);

            final SearchCommunityDto first = dtos.get(0);
            assertThat(first.getId()).isEqualTo(community1.getId());

            assertThat(first.getHashtags()).containsExactlyInAnyOrder("안녕", "ㅎㅎ");
            assertThat(first.getCategory()).isEqualTo(CommunityCategory.ACADEMIC.toString());
            assertThat(first.getDescription()).isEqualTo(BASEBALL.description);
            assertThat(first.getMemberCount()).isEqualTo(BASEBALL.memberCount);
            assertThat(first.getName()).isEqualTo(BASEBALL.communityName);
        }

        @Test
        @DisplayName("커뮤니티 검색 키워드가 없는 경우")
        void ThereIsNoSearchKeyword() {
            //given
            communityRepository.saveAll(List.of(community1, community2));

            community1.addTags(List.of("안녕", "ㅎㅎ"));
            community2.addTags(List.of("zz"));

            final CommunityQueryRequest request =
                    new CommunityQueryRequest(CommunityCategory.ACADEMIC, false, CommunityListingOrder.NEWER, null);

            cleanPersistenceContext();

            //when
            final Slice<SearchCommunityDto> slice =
                    communityRepository.getSearchedCommunities(PageRequest.of(0, 2), request);

            //then
            final List<SearchCommunityDto> dtos = slice.getContent();
            assertThat(dtos).hasSize(2)
                    .extracting("id", "name", "description", "createdAt", "hashtags",
                            "memberCount", "category", "privated")
                    .containsExactly(
                            tuple(
                                    community1.getId(), BASEBALL.communityName, BASEBALL.description, BASEBALL.createdAt, extractCommunityTags(community1.getHashtags()),
                                    BASEBALL.memberCount, BASEBALL.communityCategory.toString(), BASEBALL.isPrivate),
                            tuple(community2.getId(), POCS.communityName, POCS.description, POCS.createdAt, extractCommunityTags(community2.getHashtags()),
                                    POCS.memberCount, POCS.communityCategory.toString(), POCS.isPrivate)
                    );
        }

        private List<String> extractCommunityTags(List<CommunityHashtag> communityHashtags) {
            return communityHashtags.stream()
                    .map(CommunityHashtag::getTag)
                    .collect(Collectors.toList());
        }

        //todo: 검색 테스트 케이스 추가
    }
}