package boogi.apiserver.domain.report.dto;

import boogi.apiserver.domain.report.domain.ReportReason;
import boogi.apiserver.domain.report.domain.ReportTarget;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
public class CreateReport {

    @NotNull
    private Long id;

    @NotEmpty
    private ReportTarget target;

    @NotNull
    private ReportReason reportReason;

    private String content;

    public CreateReport(Long id, String target, String reportReason, String content) {
        this.id = id;
        try {
            this.target = ReportTarget.valueOf(target);
            this.reportReason = ReportReason.valueOf(reportReason);
        } catch(IllegalArgumentException e){
            throw new InvalidValueException("잘못된 요청입니다");
        }
        this.content = content;
    }
}
