package boogi.apiserver.domain.report.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ReportTarget {
    COMMUNITY, POST, COMMENT, MESSAGE;

    @JsonCreator
    public static ReportTarget from(String s) {
        return ReportTarget.valueOf(s.toUpperCase());
    }
}
