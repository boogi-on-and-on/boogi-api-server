package boogi.apiserver.domain.hashtag.exception;

import boogi.apiserver.domain.hashtag.domain.Tag;
import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidTagException extends InvalidValueException {

    private static final String MESSAGE = "태그는 공백이 없으며, " + Tag.MIN_LENGTH + " ~ " + Tag.MAX_LENGTH + " 길이의 영어나 한글이어야 합니다.";

    public InvalidTagException() {
        super(MESSAGE);
    }
}
