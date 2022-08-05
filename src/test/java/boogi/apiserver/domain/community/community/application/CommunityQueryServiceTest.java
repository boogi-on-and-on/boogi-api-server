package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommunityQueryServiceTest {

    @Mock
    CommunityRepository communityRepository;

    @InjectMocks
    CommunityQueryService communityQueryService;

    @Test
    @DisplayName("커뮤니티 기본 정보 전달")
    void communityBasicInfo() {
        //given
        Community community = Community.builder()
                .id(1L)
                .hashtags(List.of(CommunityHashtag.builder().build()))
                .build();

        given(communityRepository.findById(anyLong()))
                .willReturn(Optional.of(community));

        //when
        Community communityWithHashTag = communityQueryService.getCommunityWithHashTag(anyLong());
        assertThat(communityWithHashTag).isEqualTo(community);
    }
}