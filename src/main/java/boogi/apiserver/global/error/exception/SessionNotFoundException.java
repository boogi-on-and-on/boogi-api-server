package boogi.apiserver.global.error.exception;

public class SessionNotFoundException extends InvalidValueException {
    public SessionNotFoundException() {
        super(ErrorInfo.SESSION_NOT_FOUND_EXCEPTION);
    }
}
