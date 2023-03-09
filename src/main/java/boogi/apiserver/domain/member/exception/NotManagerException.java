package boogi.apiserver.domain.member.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class NotManagerException extends InvalidValueException {

    public static final String MESSAGE = "매니저가 아닙니다.";

    public NotManagerException() {
        super(MESSAGE);

    }
}
