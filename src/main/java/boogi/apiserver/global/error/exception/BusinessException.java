package boogi.apiserver.global.error.exception;

import lombok.Getter;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorInfo errorInfo;

    public BusinessException() {
        super(ErrorInfo.INTERNAL_SERVER_ERROR.getMessage());
        this.errorInfo = ErrorInfo.INTERNAL_SERVER_ERROR;
    }

    public BusinessException(ErrorInfo errorInfo) {
        super(errorInfo.getMessage());
        this.errorInfo = errorInfo;
    }

    public BusinessException(String message, ErrorInfo errorInfo) {
        super(message); //console...
        this.errorInfo = errorInfo;
        errorInfo.setMessage(message);
    }
}
