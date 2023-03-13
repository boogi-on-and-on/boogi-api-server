package boogi.apiserver.domain.message.message.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class NotParticipatedUserException extends InvalidValueException {
    public static final String MESSAGE = "본인과의 쪽지 대화일 경우에만 신고가 가능합니다.";

    public NotParticipatedUserException() {
        super(MESSAGE);
    }
}
