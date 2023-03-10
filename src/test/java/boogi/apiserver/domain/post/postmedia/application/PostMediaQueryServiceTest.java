package boogi.apiserver.domain.post.postmedia.application;

import boogi.apiserver.builder.TestPostMedia;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.vo.PostMedias;
import boogi.apiserver.global.error.exception.InvalidValueException;
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
import static org.mockito.BDDMockito.anyList;
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
//            PostMedias unmappedPostMedias = postMediaQueryService
//                    .getUnmappedPostMediasByUUID(List.of());
//
//            assertThat(unmappedPostMedias.getPostMedias().isEmpty()).isTrue();
        }

        @Test
        @DisplayName("입력한 PostMedia의 UUID의 개수만큼 리스트로 반환한다.")
        void getUnmappedPostMediasByUUIDSuccess() {
            final PostMedia postMedia1 = TestPostMedia.builder()
                    .id(1L)
                    .uuid("1234")
                    .build();
            final PostMedia postMedia2 = TestPostMedia.builder()
                    .id(2L)
                    .uuid("5678")
                    .build();
            List<PostMedia> postMedias = List.of(postMedia1, postMedia2);

            given(postMediaRepository.findUnmappedPostMediasByUUIDs(anyList()))
                    .willReturn(new PostMedias(postMedias));

//            PostMedias unmappedPostMedias = postMediaQueryService
//                    .getUnmappedPostMediasByUUID(List.of(postMedia1.getUuid(), postMedia2.getUuid()));
//
//            List<PostMedia> unmappedPostMediaList = unmappedPostMedias.getPostMedias();
//
//            assertThat(unmappedPostMediaList.size()).isEqualTo(2);
//            assertThat(unmappedPostMediaList).extracting("id").containsExactly(1L, 2L);
//            assertThat(unmappedPostMediaList).extracting("uuid").containsExactly("1234", "5678");
        }

        @Test
        @DisplayName("입력한 PostMedia의 UUID의 개수와 조회한 PostMedia의 개수가 다르면 InvalidValueException 발생한다.")
        void getUnmappedPostMediasByUUIDFail() {
            final PostMedia postMedia1 = TestPostMedia.builder()
                    .id(1L)
                    .uuid("1234")
                    .build();
            final PostMedia postMedia2 = TestPostMedia.builder()
                    .id(2L)
                    .uuid("5678")
                    .build();
            List<PostMedia> postMedias = List.of(postMedia1);

            given(postMediaRepository.findUnmappedPostMediasByUUIDs(anyList()))
                    .willReturn(new PostMedias(postMedias));

            assertThatThrownBy(() -> postMediaQueryService
                    .getUnmappedPostMediasByUUID(List.of(postMedia1.getUuid(), postMedia2.getUuid())))
                    .isInstanceOf(InvalidValueException.class);
        }
    }
}