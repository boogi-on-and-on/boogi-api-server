package boogi.apiserver.domain.user.exception;

import boogi.apiserver.domain.user.domain.Username;
import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidUsernameException extends InvalidValueException {
    private static final String MESSAGE = Username.MIN_LENGTH + " ~ " + Username.MAX_LENGTH + "까지 입력이 가능합니다.";

    public InvalidUsernameException() {
        super(MESSAGE);
    }
}
