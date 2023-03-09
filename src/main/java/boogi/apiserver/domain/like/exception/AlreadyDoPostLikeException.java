package boogi.apiserver.domain.like.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class AlreadyDoPostLikeException extends InvalidValueException {

    private static final String MESSAGE = "이미 해당 글에 좋아요를 한 상태입니다";

    public AlreadyDoPostLikeException() {
        super(MESSAGE);
    }
}
