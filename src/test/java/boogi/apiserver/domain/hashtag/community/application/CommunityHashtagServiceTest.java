package boogi.apiserver.domain.hashtag.community.application;

import boogi.apiserver.domain.community.community.application.CommunityCoreService;
import boogi.apiserver.domain.hashtag.community.dao.CommunityHashtagRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommunityHashtagServiceTest {

    @Mock
    CommunityCoreService communityCoreService;

    @Mock
    CommunityHashtagRepository communityHashtagRepository;

    @InjectMocks
    CommunityHashtagCoreService communityHashtagCoreService;

//    @Test
//    void 커뮤니티_카테고리_저장하기() {
//        //given
//        Community community = Community.builder()
//                .id(1L)
//                .build();
//        given(communityCoreService.getCommunity(anyLong()))
//                .willReturn(community);
//
//        List<String> tags = List.of("테그1", "테그2");
//        CommunityHashtag hashtag1 = CommunityHashtag.builder()
//                .tag("테그1")
//                .community(community)
//                .build();
//        CommunityHashtag hashtag2 = CommunityHashtag.builder()
//                .tag("테그2")
//                .community(community)
//                .build();
//        given(communityHashtagRepository.saveAll(any()))
//                .willReturn(List.of(hashtag1, hashtag2));
//
//        //when
//        communityHashtagCoreService.addTags(community.getId(), tags);
//
//        //then
//        assertThat(hashtag1.getTag()).isEqualTo(tags.get(0));
//        assertThat(hashtag2.getTag()).isEqualTo(tags.get(1));
//    }

}