package boogi.apiserver.domain.hashtag.post.application;

import boogi.apiserver.domain.hashtag.post.dao.PostHashtagRepository;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class PostHashtagCoreServiceTest {

    @InjectMocks
    PostHashtagCoreService postHashtagCoreService;

    @Mock
    private PostQueryService postQueryService;

    @Mock
    private PostHashtagRepository postHashtagRepository;


    @Nested
    @DisplayName("글에 해시태그 추가시")
    class AddPostHashTagTest {

        @Test
        @DisplayName("입력된 태그가 빈 리스트나 null일시 null을 반환한다.")
        void addEmptyTagsListSuccess() {
            List<PostHashtag> postHashtags1 = postHashtagCoreService.addTags(1L, List.of());
            List<PostHashtag> postHashtags2 = postHashtagCoreService.addTags(1L, null);

            assertThat(postHashtags1).isNull();
            assertThat(postHashtags2).isNull();
        }

        @Test
        @DisplayName("태그들과 postId를 입력하면 해시태그들이 추가된 후 해시태그들을 반환한다.")
        void addTagsSuccess() {
            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            given(postQueryService.getPost(anyLong()))
                    .willReturn(post);

            List<String> tags = List.of("tag1", "tag2");
            List<PostHashtag> postHashtags = postHashtagCoreService.addTags(1L, tags);

            assertThat(postHashtags.size()).isEqualTo(2);
            assertThat(postHashtags.get(0).getPost().getId()).isEqualTo(post.getId());
            assertThat(postHashtags.get(0).getTag()).isEqualTo(tags.get(0));
            assertThat(postHashtags.get(1).getPost().getId()).isEqualTo(post.getId());
            assertThat(postHashtags.get(1).getTag()).isEqualTo(tags.get(1));
        }
    }
}