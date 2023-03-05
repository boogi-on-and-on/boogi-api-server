package boogi.apiserver.domain.hashtag.domain;

import boogi.apiserver.domain.hashtag.exception.InvalidTagException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TagTest {

    static Stream<Arguments> invalidLength() {
        return Stream.of(
                Arguments.of("A".repeat(11)),
                Arguments.of(""),
                Arguments.of("   ")
        );
    }

    @DisplayName("입력값의 길이가 1 ~ 10 밖이거나 입력값이 Null이나 빈 문자열인 경우 실패")
    @ParameterizedTest(name = "{0}로 태그 생성시 예외가 발생한다.")
    @NullSource
    @MethodSource("invalidLength")
    void createLengthFail(String tag) {
        assertThatThrownBy(() -> new Tag(tag))
                .isInstanceOf(InvalidTagException.class);
    }

    @DisplayName("최소 1개의 한글이나 영어로만 구성되어 있지 않을 경우 실패")
    @ParameterizedTest(name = "{0}로 태그를 생성한다.")
    @CsvSource({"1234", "!@#$", "태그!!", "tag~"})
    void createWrongLetterFail(String tag) {
        assertThatThrownBy(() -> new Tag(tag))
                .isInstanceOf(InvalidTagException.class);
    }

    @DisplayName("입력값의 길이가 올바르고, 최소 1개 이상의 한글이나 영어로만 구성되면 성공")
    @ParameterizedTest(name = "{0}로 태그 객체 생성시 성공한다.")
    @CsvSource({"태그", "tag", "태그tag"})
    void createSuccess(String tag) {
        Tag createdTag = new Tag(tag);
        assertThat(createdTag.getValue()).isEqualTo(tag);
    }
}