package boogi.apiserver.global.error;

import boogi.apiserver.global.error.exception.BusinessException;
import boogi.apiserver.global.error.exception.ErrorInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.UnexpectedTypeException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 요청한 경로에 대한 Http Method를 제공하지 않을 경우
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<BasicErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("handleHttpRequestMethodNotSupportedException", e);

        final BasicErrorResponse response = BasicErrorResponse.of(ErrorInfo.RESOURCE_NOT_FOUND);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Bean Validation 등에 실패한 경우
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, UnexpectedTypeException.class, HttpMessageNotReadableException.class})
    protected ResponseEntity<BasicErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("handleMethodArgumentNotValidException", e);

        final BasicErrorResponse response = BasicErrorResponse.of(ErrorInfo.BAD_REQUEST);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * RequestParam binding 실패한 경우
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<BasicErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("handleMethodArgumentTypeMismatchException", e);

        final BasicErrorResponse response = BasicErrorResponse.of(ErrorInfo.BAD_REQUEST);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BusinessException.class)
    private ResponseEntity<BasicErrorResponse> handleBusinessException(BusinessException e) {
        log.error("handleBusinessException", e);

        final ErrorInfo errorInfo = e.getErrorInfo();
        BasicErrorResponse response = BasicErrorResponse.of(errorInfo);
        int statusCode = errorInfo.getStatusCode();
        return new ResponseEntity<>(response, HttpStatus.valueOf(statusCode));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<BasicErrorResponse> handleException(Exception e) {
        log.error("handleException", e);

        final BasicErrorResponse response = BasicErrorResponse.of(ErrorInfo.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
