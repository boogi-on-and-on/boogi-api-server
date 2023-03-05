package boogi.apiserver.builder;

import boogi.apiserver.domain.report.domain.Report;

public class TestReport {

    public static Report.ReportBuilder builder() {
        return Report.builder()
                .content("테스트 신고 내용");
    }
}
