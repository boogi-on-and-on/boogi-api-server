package boogi.apiserver.domain.message.message.exception;

import boogi.apiserver.domain.message.message.domain.Content;
import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidMessageContentException extends InvalidValueException {
    private static final String MESSAGE = "메시지의 길이는 " + Content.MIN_LENGTH + " ~ " + Content.MAX_LENGTH + "까지 입력이 가능합니다.";

    public InvalidMessageContentException() {
        super(MESSAGE);
    }
}
