package boogi.apiserver.global.error.exception;

public class EntityNotFoundException extends BusinessException {
    public EntityNotFoundException() {
        super(ErrorInfo.NOT_FOUND);
    }

    public EntityNotFoundException(String message) {
        super(message, ErrorInfo.NOT_FOUND);
    }

    public EntityNotFoundException(String message, ErrorInfo errorInfo) {
        super(message, errorInfo);
    }

    public EntityNotFoundException(ErrorInfo errorInfo) {
        super(errorInfo);
    }
}
