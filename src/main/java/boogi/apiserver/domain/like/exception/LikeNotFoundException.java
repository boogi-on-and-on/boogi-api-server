package boogi.apiserver.domain.like.exception;

import boogi.apiserver.global.error.exception.EntityNotFoundException;

public class LikeNotFoundException extends EntityNotFoundException {
    public static final String MESSAGE = "좋아요를 찾을 수 없습니다.";

    public LikeNotFoundException() {
        super(MESSAGE);
    }
}
