package boogi.apiserver.domain.post.postmedia.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class UnmappedPostMediaExcecption extends InvalidValueException {

    private static final String MESSAGE = "게시글 미디어를 등록하는 도중에 에러가 발생했습니다.";

    public UnmappedPostMediaExcecption() {
        super(MESSAGE);
    }
}
