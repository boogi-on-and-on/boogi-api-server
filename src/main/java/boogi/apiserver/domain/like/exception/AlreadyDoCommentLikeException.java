package boogi.apiserver.domain.like.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class AlreadyDoCommentLikeException extends InvalidValueException {
    private static final String MESSAGE = "이미 해당 댓글에 좋아요를 한 상태입니다";

    public AlreadyDoCommentLikeException() {
        super(MESSAGE);
    }
}
