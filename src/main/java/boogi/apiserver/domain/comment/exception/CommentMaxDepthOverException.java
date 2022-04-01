package boogi.apiserver.domain.comment.exception;

import boogi.apiserver.global.error.exception.ErrorInfo;
import boogi.apiserver.global.error.exception.InvalidValueException;

public class CommentMaxDepthOverException extends InvalidValueException {
    public CommentMaxDepthOverException() {
        super(ErrorInfo.COMMENT_MAX_DEPTH_OVER);
    }
}
