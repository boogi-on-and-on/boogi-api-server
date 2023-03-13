package boogi.apiserver.domain.community.joinrequest.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class NotPendingJoinRequestException extends InvalidValueException {
    private static final String MESSAGE = "승인 대기중인 요청이 아닙니다.";

    public NotPendingJoinRequestException() {
        super(MESSAGE);
    }
}
