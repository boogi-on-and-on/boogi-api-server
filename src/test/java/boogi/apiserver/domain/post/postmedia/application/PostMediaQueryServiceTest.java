package boogi.apiserver.domain.post.postmedia.application;

import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.global.error.exception.InvalidValueException;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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
            List<PostMedia> unmappedPostMedias = postMediaQueryService
                    .getUnmappedPostMediasByUUID(List.of());

            assertThat(unmappedPostMedias.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("입력한 PostMedia의 UUID의 개수만큼 리스트로 반환한다.")
        void getUnmappedPostMediasByUUIDSuccess() {
            final PostMedia postMedia1 = TestEmptyEntityGenerator.PostMedia();
            ReflectionTestUtils.setField(postMedia1, "id", 1L);
            ReflectionTestUtils.setField(postMedia1, "uuid", "1234");

            final PostMedia postMedia2 = TestEmptyEntityGenerator.PostMedia();
            ReflectionTestUtils.setField(postMedia2, "id", 2L);
            ReflectionTestUtils.setField(postMedia2, "uuid", "5678");

            List<PostMedia> postMedias = List.of(postMedia1, postMedia2);

            given(postMediaRepository.findUnmappedPostMediasByUUIDs(anyList()))
                    .willReturn(postMedias);

            List<PostMedia> unmappedPostMedias = postMediaQueryService
                    .getUnmappedPostMediasByUUID(List.of(postMedia1.getUuid(), postMedia2.getUuid()));

            assertThat(unmappedPostMedias.size()).isEqualTo(2);
            assertThat(unmappedPostMedias.get(0).getId()).isEqualTo(postMedia1.getId());
            assertThat(unmappedPostMedias.get(0).getUuid()).isEqualTo(postMedia1.getUuid());
            assertThat(unmappedPostMedias.get(1).getId()).isEqualTo(postMedia2.getId());
            assertThat(unmappedPostMedias.get(1).getUuid()).isEqualTo(postMedia2.getUuid());
        }

        @Test
        @DisplayName("입력한 PostMedia의 UUID의 개수와 조회한 PostMedia의 개수가 다르면 InvalidValueException 발생한다.")
        void getUnmappedPostMediasByUUIDFail() {
            final PostMedia postMedia1 = TestEmptyEntityGenerator.PostMedia();
            ReflectionTestUtils.setField(postMedia1, "id", 1L);
            ReflectionTestUtils.setField(postMedia1, "uuid", "1234");

            final PostMedia postMedia2 = TestEmptyEntityGenerator.PostMedia();
            ReflectionTestUtils.setField(postMedia2, "id", 2L);
            ReflectionTestUtils.setField(postMedia2, "uuid", "5678");

            List<PostMedia> postMedias = List.of(postMedia1);

            given(postMediaRepository.findUnmappedPostMediasByUUIDs(anyList()))
                    .willReturn(postMedias);

            assertThatThrownBy(() -> postMediaQueryService
                    .getUnmappedPostMediasByUUID(List.of(postMedia1.getUuid(), postMedia2.getUuid())))
                    .isInstanceOf(InvalidValueException.class);
        }
    }
}