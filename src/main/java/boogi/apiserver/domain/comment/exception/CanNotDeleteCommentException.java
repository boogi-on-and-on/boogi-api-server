package boogi.apiserver.domain.comment.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class CanNotDeleteCommentException extends InvalidValueException {

    private final static String MESSAGE = "해당 댓글의 삭제 권한이 없습니다.";
    
    public CanNotDeleteCommentException() {
        super(MESSAGE);
    }
}
