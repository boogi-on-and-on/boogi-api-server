package boogi.apiserver.domain.message.block.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class NotBlockedUserException extends InvalidValueException {
    public static final String MESSAGE = "차단되지 않은 유저입니다.";

    public NotBlockedUserException() {
        super(MESSAGE);
    }
}
