package boogi.apiserver.domain.report.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidReportReasonException extends InvalidValueException {
    private static final String MESSAGE = "잘못된 신고 사유입니다.";

    public InvalidReportReasonException() {
        super(MESSAGE);
    }
}
