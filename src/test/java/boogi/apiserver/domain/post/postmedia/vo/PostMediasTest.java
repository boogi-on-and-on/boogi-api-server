package boogi.apiserver.domain.post.postmedia.vo;

import boogi.apiserver.builder.TestPostMedia;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostMediasTest {

    private PostMedias postMedias;

    @BeforeAll
    void init() {
        final PostMedia postMedia1 = TestPostMedia.builder().uuid("123").build();
        final PostMedia postMedia2 = TestPostMedia.builder().uuid("456").build();
        final PostMedia postMedia3 = TestPostMedia.builder().uuid("789").build();

        postMedias = new PostMedias(
                List.of(postMedia1, postMedia2, postMedia3)
        );
    }

    @DisplayName("PostMedias에서 동시에 존재하는 값을 UUID 기준으로 삭제한다.")
    @ParameterizedTest(name = "{0}를 입력했을때 {1}이 반환된다.")
    @CsvSource(value = {"[123,111,222]:[456,789]", "[123,456,111]:[789]", "[123,456,789]:[]"}, delimiter = ':')
    void excludedPostMediaTest(String rawUuids, String expectedRawUuids) {
        List<String> uuids = convertToUuidsFromRaw(rawUuids);
        List<String> expectedUuids = convertToUuidsFromRaw(expectedRawUuids);

        List<PostMedia> excludedPostMedia = postMedias.excludedPostMedia(uuids);
        List<String> excludedPostMediaIds = convertToUuids(excludedPostMedia);

        assertThat(excludedPostMediaIds).isEqualTo(expectedUuids);
    }

    @DisplayName("입력된 UUID에서 PostMedias에 동시에 존재하지 않는 값만 가져온다.")
    @ParameterizedTest(name = "{0}를 입력했을때 {1}이 반환된다.")
    @CsvSource(value = {"[123,111,222]:[111,222]", "[123,456,111]:[111]", "[123,456,789]:[]"}, delimiter = ':')
    void newPostMediaTest(String rawUuids, String expectedRawUuids) {
        List<String> uuids = convertToUuidsFromRaw(rawUuids);
        List<String> expectedUuids = convertToUuidsFromRaw(expectedRawUuids);

        List<String> newPostMediaIds = postMedias.newPostMediaIds(uuids);

        assertThat(newPostMediaIds).isEqualTo(expectedUuids);
    }

    private static List<String> convertToUuidsFromRaw(String rawUuids) {
        if (rawUuids.equals("[]")) {
            return List.of();
        }
        rawUuids = rawUuids.substring(1, rawUuids.length() - 1);
        return Arrays.asList(rawUuids.split(","));
    }

    private static List<String> convertToUuids(List<PostMedia> postMedias) {
        return postMedias.stream()
                .map(PostMedia::getUuid)
                .collect(Collectors.toList());
    }
}