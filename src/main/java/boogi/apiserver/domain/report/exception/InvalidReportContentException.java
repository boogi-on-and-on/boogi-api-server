package boogi.apiserver.domain.report.exception;

import boogi.apiserver.domain.report.domain.ReportContent;
import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidReportContentException extends InvalidValueException {
    private static final String MESSAGE = "신고의 내용 길이는 " + ReportContent.MIN_LENGTH + " ~ " + ReportContent.MAX_LENGTH + "까지 입력이 가능합니다.";

    public InvalidReportContentException() {
        super(MESSAGE);
    }
}
