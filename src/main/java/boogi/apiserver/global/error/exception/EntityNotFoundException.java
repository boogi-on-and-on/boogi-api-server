package boogi.apiserver.global.error.exception;

public class EntityNotFoundException extends BusinessException {
    public EntityNotFoundException() {
        super(ErrorInfo.COMMON_NOT_FOUND);
    }

    public EntityNotFoundException(String message) {
        super(message, ErrorInfo.COMMON_NOT_FOUND);
    }

    public EntityNotFoundException(String message, ErrorInfo errorInfo) {
        super(message, errorInfo);
    }

    public EntityNotFoundException(ErrorInfo errorInfo) {
        super(errorInfo);
    }
}
