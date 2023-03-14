package boogi.apiserver.domain.post.postmedia.application;

import boogi.apiserver.builder.TestPostMedia;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.exception.UnmappedPostMediaExcecption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PostMediaQueryServiceTest {

    @InjectMocks
    private PostMediaQueryService postMediaQueryService;

    @Mock
    private PostMediaRepository postMediaRepository;


    @Nested
    @DisplayName("postId가 세팅되지 않은 PostMedia들을 UUID로 조회할시")
    class GetUnmappedPostMediasByUUIDTest {
        private static final String POSTMEDIA1_UUID = "1234";
        private static final String POSTMEDIA2_UUID = "5678";

        @Test
        @DisplayName("입력한 PostMedia의 UUID가 없을때 빈 리스트를 반환한다.")
        void getUnmappedPostMediasByUUIDEmptyArrSuccess() {
            List<PostMedia> unmappedPostMedias = postMediaQueryService.getUnmappedPostMediasByUUID(List.of());

            assertThat(unmappedPostMedias.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("입력한 PostMedia의 UUID의 개수만큼 리스트로 반환한다.")
        void getUnmappedPostMediasByUUIDSuccess() {
            final PostMedia postMedia1 = TestPostMedia.builder()
                    .id(1L)
                    .uuid(POSTMEDIA1_UUID)
                    .build();
            final PostMedia postMedia2 = TestPostMedia.builder()
                    .id(2L)
                    .uuid(POSTMEDIA2_UUID)
                    .build();
            List<PostMedia> postMedias = List.of(postMedia1, postMedia2);

            given(postMediaRepository.findUnmappedPostMedias(anyList()))
                    .willReturn(postMedias);

            List<PostMedia> unmappedPostMedias = postMediaQueryService
                    .getUnmappedPostMediasByUUID(List.of(POSTMEDIA1_UUID, POSTMEDIA2_UUID));

            assertThat(unmappedPostMedias).hasSize(2);
            assertThat(unmappedPostMedias).extracting("id").containsExactly(1L, 2L);
            assertThat(unmappedPostMedias).extracting("uuid")
                    .containsExactly(POSTMEDIA1_UUID, POSTMEDIA2_UUID);
        }

        @Test
        @DisplayName("입력한 PostMedia의 UUID의 개수와 조회한 PostMedia의 개수가 다르면 UnmappedPostMediaExcecption 발생한다.")
        void getUnmappedPostMediasByUUIDFail() {
            final PostMedia postMedia = TestPostMedia.builder()
                    .id(1L)
                    .uuid(POSTMEDIA1_UUID)
                    .build();

            given(postMediaRepository.findUnmappedPostMedias(anyList()))
                    .willReturn(List.of(postMedia));

            assertThatThrownBy(() -> postMediaQueryService
                    .getUnmappedPostMediasByUUID(List.of(POSTMEDIA1_UUID, POSTMEDIA2_UUID)))
                    .isInstanceOf(UnmappedPostMediaExcecption.class);
        }
    }
}