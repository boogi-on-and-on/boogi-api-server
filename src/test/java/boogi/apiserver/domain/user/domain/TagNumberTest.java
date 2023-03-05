package boogi.apiserver.domain.user.domain;

import boogi.apiserver.domain.user.exception.InvalidTagNumberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class TagNumberTest {

    @DisplayName("입력값의 길이가 5인 문자열이 아니면 실패")
    @ParameterizedTest(name = "{0}로 태그 번호 생성시 예외가 발생한다.")
    @NullSource
    @CsvSource({"#", "#1", "#12", "#123", "#12345"})
    void createLengthFail(String tagNumber) {
        assertThatThrownBy(() -> new TagNumber(tagNumber))
                .isInstanceOf(InvalidTagNumberException.class);
    }

    @DisplayName("#으로 시작하고, 0001 ~ 9999 사이가 아닌 경우 실패")
    @ParameterizedTest(name = "{0}로 태그 번호 생성시 예외가 발생한다.")
    @CsvSource({"$1234", "1234#", "#####", "#01", "#    ", "#0000"})
    void createWrongFormatFail(String tagNumber) {
        assertThatThrownBy(() -> new TagNumber(tagNumber))
                .isInstanceOf(InvalidTagNumberException.class);
    }

    @Test
    @DisplayName("#0001 ~ #9999 사이인 경우 성공")
    void createSuccess() {
        TagNumber tagNumber = new TagNumber("#1234");
        assertThat(tagNumber.getValue()).isEqualTo("#1234");
    }
}