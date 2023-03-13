package boogi.apiserver.domain.comment.exception;

import boogi.apiserver.global.error.exception.EntityNotFoundException;

public class CommentNotFoundException extends EntityNotFoundException {

    private static final String MESSAGE = "해당 댓글이 존재하지 않습니다.";

    public CommentNotFoundException() {
        super(MESSAGE);
    }
}
