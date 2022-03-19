package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.LatestPostOfUserJoinedCommunity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PostQueryServiceTest {

    @Mock
    PostRepository postRepository;

    @InjectMocks
    PostQueryService postQueryService;

    //todo: Page 객체 직접 생성해서 테스트하기

    @Test
    void 유저가_가입한_최근_커뮤니티_글() {
        //given
        Community community = Community.builder()
                .id(1L)
                .communityName("커뮤니티1")
                .build();

        Post post = Post.builder()
                .id(2L)
                .community(community)
                .content("글")
                .likeCount(1)
                .commentCount(2)
                .hashtags(List.of(PostHashtag.builder().tag("해시테그").build()))
                .build();
        post.setCreatedAt(LocalDateTime.now());

        //when
        given(postRepository.getLatestPostOfCommunity(anyLong()))
                .willReturn(List.of(post));

        //then
        List<LatestPostOfUserJoinedCommunity> postDtos = postQueryService.getPostsOfUserJoinedCommunity(anyLong());

        LatestPostOfUserJoinedCommunity dto = postDtos.get(0);

        assertThat(dto.getId()).isEqualTo("1");
        assertThat(dto.getName()).isEqualTo("커뮤니티1");
        assertThat(dto.getPost().getId()).isEqualTo("2");

    }
}