package boogi.apiserver.domain.hashtag.post.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class PostHashtagsTest {

    @DisplayName("해시태그를 6개 이상 추가나 변경하는 경우 실패")
    @Test
    void addAndUpdateSizeFail() {
        List<String> tags = IntStream.rangeClosed(1, 6)
                .mapToObj(i -> "해시태그" + i)
                .collect(Collectors.toList());

        PostHashtags postHashtags = new PostHashtags();

        assertThatThrownBy(() -> postHashtags.addTags(tags, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글 해시태그는 최대 5개 까지만 추가 가능합니다.");
        assertThatThrownBy(() -> postHashtags.updateTags(tags, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글 해시태그는 최대 5개 까지만 추가 가능합니다.");
    }

    @DisplayName("해시태그 추가나 변경시 null을 입력하면 성공한다.")
    @Test
    void nullSuccess() {
        PostHashtags postHashtags = new PostHashtags();

        postHashtags.addTags(null, null);
        assertThat(postHashtags.getValues()).isEqualTo(new ArrayList<>());

        postHashtags.updateTags(null, null);
        assertThat(postHashtags.getValues()).isEqualTo(new ArrayList<>());
    }
}