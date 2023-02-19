package boogi.apiserver.domain.user.exception;


import boogi.apiserver.domain.user.domain.Introduce;
import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidIntroduceException extends InvalidValueException {

    private static final String MESSAGE = "소개글의 길이는 " + Introduce.MIN_LENGTH + " ~ " + Introduce.MAX_LENGTH + "까지 입력이 가능합니다.";

    public InvalidIntroduceException() {
        super(MESSAGE);
    }
}
