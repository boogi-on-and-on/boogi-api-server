package boogi.apiserver.domain.community.community.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class CanNotDeleteCommunityException extends InvalidValueException {
    private final static String MESSAGE = "탈퇴하지 않은 부매니저 혹은 일반 맴버가 있습니다.";
    public CanNotDeleteCommunityException() {
        super(MESSAGE);
    }
}
