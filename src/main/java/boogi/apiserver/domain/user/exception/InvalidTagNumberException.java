package boogi.apiserver.domain.user.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidTagNumberException extends InvalidValueException {
    private static final String MESSAGE = "태그 번호는 #0001 ~ #9999사이여야 합니다.";

    public InvalidTagNumberException() {
        super(MESSAGE);
    }
}
