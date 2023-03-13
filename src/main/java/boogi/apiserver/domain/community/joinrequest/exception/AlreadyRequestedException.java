package boogi.apiserver.domain.community.joinrequest.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class AlreadyRequestedException extends InvalidValueException {
    private final static String MESSAGE = "이미 가입요청을 한 커뮤니티입니다.";

    public AlreadyRequestedException() {
        super(MESSAGE);
    }
}
