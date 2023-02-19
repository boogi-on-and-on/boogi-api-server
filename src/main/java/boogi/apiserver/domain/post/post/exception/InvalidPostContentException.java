package boogi.apiserver.domain.post.post.exception;

import boogi.apiserver.domain.post.post.domain.Content;
import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidPostContentException extends InvalidValueException {
    private static final String MESSAGE = "게시글의 내용 길이는 " + Content.MIN_LENGTH + " ~ " + Content.MAX_LENGTH + "까지 입력이 가능합니다.";

    public InvalidPostContentException() {
        super(MESSAGE);
    }
}
