package boogi.apiserver.domain.user.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class WithdrawnOrCanceledUserException extends InvalidValueException {
    public WithdrawnOrCanceledUserException() {
        super("탈퇴하거나, 삭제된 유저입니다.");
    }
}
