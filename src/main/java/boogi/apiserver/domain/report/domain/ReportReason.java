package boogi.apiserver.domain.report.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ReportReason {
    SEXUAL("음란물"),
    SWEAR("욕설"),
    DEFAMATION("명예훼손"),
    POLITICS("정치인 비하 및 선거운동"),
    COMMERCIAL_AD("상업적 광고"),
    ILLEGAL_FILMING("불법 촬영물"),
    ETC("기타");

    private String description;

    ReportReason(String description) {
        this.description = description;
    }

    @JsonCreator
    public static ReportReason from(String s) {
        return ReportReason.valueOf(s.toUpperCase());
    }
}
