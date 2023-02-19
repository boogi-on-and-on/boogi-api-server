package boogi.apiserver.domain.user.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidEmailException extends InvalidValueException {
    private static final String MESSAGE = "이메일 형식이 올바르지 않습니다.";

    public InvalidEmailException() {
        super(MESSAGE);
    }
}
