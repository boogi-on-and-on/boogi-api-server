package boogi.apiserver.domain.comment.exception;

import boogi.apiserver.domain.comment.domain.Content;
import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidCommentContentException extends InvalidValueException {
    private static final String MESSAGE = Content.MIN_LENGTH + " ~ " + Content.MAX_LENGTH + "까지 입력이 가능합니다.";

    public InvalidCommentContentException() {
        super(MESSAGE);
    }
}
