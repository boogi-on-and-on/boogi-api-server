package boogi.apiserver.domain.community.community.exception;

import boogi.apiserver.domain.community.community.domain.Description;
import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidDescriptionException extends InvalidValueException {
    private static final String MESSAGE = "커뮤니티 소개란은 "+ Description.MIN_LENGTH + " ~ " + Description.MAX_LENGTH + "까지 입력이 가능합니다.";

    public InvalidDescriptionException() {
        super(MESSAGE);
    }
}
