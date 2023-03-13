package boogi.apiserver.domain.post.post.exception;

import boogi.apiserver.global.error.exception.EntityNotFoundException;

public class PostNotFoundException extends EntityNotFoundException {

    private static final String MESSAGE = "게시글을 찾을 수 없습니다.";

    public PostNotFoundException() {
        super(MESSAGE);
    }
}
