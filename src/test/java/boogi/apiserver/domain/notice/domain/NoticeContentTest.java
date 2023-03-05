package boogi.apiserver.domain.notice.domain;

import boogi.apiserver.domain.notice.exception.InvalidNoticeContentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class NoticeContentTest {

    static Stream<Arguments> invalidLength() {
        return Stream.of(
                Arguments.of("A".repeat(9)),
                Arguments.of("A".repeat(1001)),
                Arguments.of(""),
                Arguments.of(" ".repeat(11))
        );
    }

    @DisplayName("입력값의 길이가 10 ~ 1000 밖이거나 입력값이 Null이나 공백으로만 이루어진 문자열인 경우 실패")
    @ParameterizedTest(name = "{0}로 공지사항 내용 생성시 예외가 발생한다.")
    @NullSource
    @MethodSource("invalidLength")
    void createLengthFail(String content) {
        assertThatThrownBy(() -> new NoticeContent(content))
                .isInstanceOf(InvalidNoticeContentException.class);
    }

    @Test
    @DisplayName("입력값의 앞뒤 공백 문자를 제거")
    void trimSuccess() {
        String BEFORE_TRIM = "      공지사항 내용입니다.    ";
        NoticeContent noticeContent = new NoticeContent(BEFORE_TRIM);
        assertThat(noticeContent.getValue()).isEqualTo(BEFORE_TRIM.trim());
    }

    @Test
    @DisplayName("입력값의 길이가 올바르고 공백으로만 이루어진 문자열이 아니면 성공")
    void createSuccess() {
        NoticeContent noticeContent = new NoticeContent("공지사항 내용입니다.");
        assertThat(noticeContent.getValue()).isEqualTo("공지사항 내용입니다.");
    }
}