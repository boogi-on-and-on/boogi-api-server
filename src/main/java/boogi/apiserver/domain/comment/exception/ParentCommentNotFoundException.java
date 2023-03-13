package boogi.apiserver.domain.comment.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class ParentCommentNotFoundException extends InvalidValueException {
    private final static String MESSAGE = "해당 댓글의 부모댓글을 찾을 수 없습니다.";
    public ParentCommentNotFoundException() {
        super(MESSAGE);
    }
}
