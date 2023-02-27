package boogi.apiserver.domain.user.exception;

import boogi.apiserver.domain.user.domain.Username;
import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidUsernameException extends InvalidValueException {
    private static final String MESSAGE = "유저 이름은 " + Username.MIN_LENGTH + " ~ " + Username.MAX_LENGTH +
            "길이의 한글이어야 합니다.";

    public InvalidUsernameException() {
        super(MESSAGE);
    }
}
