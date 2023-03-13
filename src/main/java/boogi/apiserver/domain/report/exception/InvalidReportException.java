package boogi.apiserver.domain.report.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidReportException extends InvalidValueException {
    private static final String MESSAGE = "잘못된 신고 대상입니다.";

    public InvalidReportException() {
        super(MESSAGE);
    }
}
