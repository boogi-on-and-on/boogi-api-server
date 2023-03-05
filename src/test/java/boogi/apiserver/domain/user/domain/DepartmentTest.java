package boogi.apiserver.domain.user.domain;

import boogi.apiserver.domain.user.exception.InvalidDepartmentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class DepartmentTest {

    static Stream<Arguments> invalidLength() {
        return Stream.of(
                Arguments.of("A".repeat(2)),
                Arguments.of("A".repeat(21)),
                Arguments.of(""),
                Arguments.of(" ".repeat(3))
        );
    }

    @DisplayName("입력값의 길이가 3 ~ 20 밖이거나 입력값이 Null이나 공백으로만 이루어진 문자열인 경우 실패")
    @ParameterizedTest(name = "{0}로 학과 생성시 예외가 발생한다.")
    @NullSource
    @MethodSource("invalidLength")
    void createLengthFail(String department) {
        assertThatThrownBy(() -> new Department(department))
                .isInstanceOf(InvalidDepartmentException.class);
    }

    @DisplayName("한글이 아닌 글자가 포함됬거나 자음이나 모음이 독립적으로 포함된 경우 실패")
    @ParameterizedTest(name = "{0}로 학과 생성시 예외가 발생한다.")
    @CsvSource({"1234", "computer", "!@#$", "ㅋㅍㅌ공학부"})
    void createWrongLetterFail(String department) {
        assertThatThrownBy(() -> new Department(department))
                .isInstanceOf(InvalidDepartmentException.class);
    }

    @Test
    @DisplayName("입력값의 앞뒤 공백 문자를 제거")
    void trimSuccess() {
        String BEFORE_TRIM = "      학과입니다    ";
        Department department = new Department(BEFORE_TRIM);
        assertThat(department.getValue()).isEqualTo(BEFORE_TRIM.trim());
    }

    @Test
    @DisplayName("입력값의 길이가 올바른 한글 문자열일 경우 성공")
    void createSuccess() {
        Department department = new Department("학과 이름");
        assertThat(department.getValue()).isEqualTo("학과 이름");
    }
}