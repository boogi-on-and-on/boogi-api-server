package boogi.apiserver.domain.member.exception;

import boogi.apiserver.global.error.exception.ErrorInfo;
import boogi.apiserver.global.error.exception.InvalidValueException;

public class NotAuthorizedMemberException extends InvalidValueException {
    public NotAuthorizedMemberException() {
        super(ErrorInfo.MEMBER_NOT_PERMITTED);
    }

    public NotAuthorizedMemberException(String message) {
        super(message, ErrorInfo.MEMBER_NOT_PERMITTED);
    }
}
