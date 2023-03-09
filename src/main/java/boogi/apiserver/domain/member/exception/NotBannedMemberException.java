package boogi.apiserver.domain.member.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class NotBannedMemberException extends InvalidValueException {
    public static final String MESSAGE = "차단되지 않은 멤버입니다.";

    public NotBannedMemberException() {
        super(MESSAGE);
    }
}
