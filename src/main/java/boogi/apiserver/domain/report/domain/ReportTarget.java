package boogi.apiserver.domain.report.domain;

import boogi.apiserver.domain.report.exception.InvalidReportTargetException;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum ReportTarget {
    COMMUNITY, POST, COMMENT, MESSAGE;

    @JsonCreator
    public static ReportTarget from(String s) {
        try {
            return ReportTarget.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidReportTargetException();
        }
    }
}
