package boogi.apiserver.domain.member.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class NotOperatorException extends InvalidValueException {
    public static final String MESSAGE = "관리자가 아닙니다.";

    public NotOperatorException() {
        super(MESSAGE);
    }
}
