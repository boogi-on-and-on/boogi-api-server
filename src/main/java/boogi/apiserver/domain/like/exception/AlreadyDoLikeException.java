package boogi.apiserver.domain.like.exception;

import boogi.apiserver.global.error.exception.ErrorInfo;
import boogi.apiserver.global.error.exception.InvalidValueException;

public class AlreadyDoLikeException extends InvalidValueException {
    public AlreadyDoLikeException() {
        super(ErrorInfo.LIKE_ALREADY_DO);
    }

    public AlreadyDoLikeException(String message) {
        super(message, ErrorInfo.LIKE_ALREADY_DO);
    }
}
