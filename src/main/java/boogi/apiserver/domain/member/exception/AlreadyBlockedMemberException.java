package boogi.apiserver.domain.member.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class AlreadyBlockedMemberException extends InvalidValueException {
    public AlreadyBlockedMemberException() {
        super();
    }

    public AlreadyBlockedMemberException(String message) {
        super(message);
    }
}
