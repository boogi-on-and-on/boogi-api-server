package boogi.apiserver.global.error.exception;

public class EntityNotFoundException extends BusinessException {
    public EntityNotFoundException() {
        super(ErrorInfo.INTERNAL_SERVER_ERROR);
    }

    public EntityNotFoundException(String message) {
        super(message, ErrorInfo.INTERNAL_SERVER_ERROR);
    }

    public EntityNotFoundException(String message, ErrorInfo errorInfo) {
        super(message, errorInfo);
    }

    public EntityNotFoundException(ErrorInfo errorInfo) {
        super(errorInfo);
    }
}
