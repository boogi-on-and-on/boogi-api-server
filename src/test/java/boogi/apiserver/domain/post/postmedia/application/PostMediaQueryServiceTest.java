package boogi.apiserver.domain.post.postmedia.application;

import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.exception.UnmappedPostMediaExcecption;
import boogi.apiserver.domain.post.postmedia.repository.PostMediaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static boogi.apiserver.utils.fixture.PostMediaFixture.POSTMEDIA1;
import static boogi.apiserver.utils.fixture.PostMediaFixture.POSTMEDIA2;
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
        @Test
        @DisplayName("입력한 PostMedia의 UUID가 없을때 빈 리스트를 반환한다.")
        void getUnmappedPostMediasByUUIDEmptyArrSuccess() {
            List<PostMedia> unmappedPostMedias = postMediaQueryService.getUnmappedPostMediasByUUID(List.of());

            assertThat(unmappedPostMedias.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("입력한 PostMedia의 UUID의 개수만큼 리스트로 반환한다.")
        void getUnmappedPostMediasByUUIDSuccess() {
            List<PostMedia> postMedias = List.of(POSTMEDIA1.toPostMedia(1L), POSTMEDIA2.toPostMedia(2L));

            given(postMediaRepository.findUnmappedPostMedias(anyList()))
                    .willReturn(postMedias);

            List<PostMedia> unmappedPostMedias = postMediaQueryService
                    .getUnmappedPostMediasByUUID(List.of(POSTMEDIA1.uuid, POSTMEDIA2.uuid));

            assertThat(unmappedPostMedias).hasSize(2);
            assertThat(unmappedPostMedias).extracting("id").containsExactly(1L, 2L);
            assertThat(unmappedPostMedias).extracting("uuid")
                    .containsExactly(POSTMEDIA1.uuid, POSTMEDIA2.uuid);
        }

        @Test
        @DisplayName("입력한 PostMedia의 UUID의 개수와 조회한 PostMedia의 개수가 다르면 UnmappedPostMediaExcecption 발생한다.")
        void getUnmappedPostMediasByUUIDFail() {
            PostMedia postMedia = POSTMEDIA1.toPostMedia(1L);

            given(postMediaRepository.findUnmappedPostMedias(anyList()))
                    .willReturn(List.of(postMedia));

            assertThatThrownBy(() -> postMediaQueryService
                    .getUnmappedPostMediasByUUID(List.of(POSTMEDIA1.uuid, POSTMEDIA2.uuid)))
                    .isInstanceOf(UnmappedPostMediaExcecption.class);
        }
    }
}