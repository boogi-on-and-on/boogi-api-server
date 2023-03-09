package boogi.apiserver.domain.like.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class UnmatchedLikeUserException extends InvalidValueException {
    public static final String MESSAGE = "요청한 유저가 좋아요를 한 유저와 다릅니다.";

    public UnmatchedLikeUserException() {
        super(MESSAGE);
    }
}
