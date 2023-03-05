package boogi.apiserver.domain.report.domain;

import boogi.apiserver.domain.report.exception.InvalidReportContentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ReportContentTest {

    static Stream<Arguments> invalidLength() {
        return Stream.of(
                Arguments.of("A".repeat(9)),
                Arguments.of("A".repeat(256)),
                Arguments.of(""),
                Arguments.of(" ".repeat(11))
        );
    }

    @DisplayName("입력값의 길이가 10 ~ 255 밖이거나 입력값이 Null이나 공백으로만 이루어진 문자열인 경우 실패")
    @ParameterizedTest(name = "{0}로 신고 내용 생성시 예외가 발생한다.")
    @NullSource
    @MethodSource("invalidLength")
    void createLengthFail(String content) {
        assertThatThrownBy(() -> new ReportContent(content))
                .isInstanceOf(InvalidReportContentException.class);
    }

    @Test
    @DisplayName("입력값의 길이가 올바르고 공백으로만 이루어진 문자열이 아니면 성공")
    void createSuccess() {
        ReportContent reportContent = new ReportContent("신고 내용 입니다.");
        assertThat(reportContent.getValue()).isEqualTo("신고 내용 입니다.");
    }
}