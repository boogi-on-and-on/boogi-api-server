package boogi.apiserver.domain.user.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidTagNumberException extends InvalidValueException {
    private static final String MESSAGE = "태그 번호는 #과 숫자 4자리로 구성되어야합니다.";

    public InvalidTagNumberException() {
        super(MESSAGE);
    }
}
