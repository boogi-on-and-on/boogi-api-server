package boogi.apiserver.global.error.exception;

public class InvalidValueException extends BusinessException {
    public InvalidValueException() {
        super(ErrorInfo.INVALID_INPUT_ERROR);
    }

    public InvalidValueException(String message) {
        super(message, ErrorInfo.INVALID_INPUT_ERROR);
    }

    public InvalidValueException(String message, ErrorInfo errorInfo) {
        super(message, errorInfo);
    }

    public InvalidValueException(ErrorInfo errorInfo){
        super(errorInfo);
    }

}
