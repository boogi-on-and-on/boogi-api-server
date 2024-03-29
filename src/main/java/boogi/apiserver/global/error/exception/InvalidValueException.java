package boogi.apiserver.global.error.exception;

public class InvalidValueException extends BusinessException {
    public InvalidValueException() {
        super(ErrorInfo.COMMON_INVALID_INPUT_ERROR);
    }

    public InvalidValueException(String message) {
        super(message, ErrorInfo.COMMON_INVALID_INPUT_ERROR);
    }

    public InvalidValueException(String message, ErrorInfo errorInfo) {
        super(message, errorInfo);
    }

    public InvalidValueException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

}
