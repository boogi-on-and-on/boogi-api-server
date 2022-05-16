package boogi.apiserver.domain.report.dto;

import boogi.apiserver.domain.report.domain.ReportReason;
import boogi.apiserver.domain.report.domain.ReportTarget;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class CreateReport {

    @NotNull
    private Long id;

    @NotNull
    private ReportTarget target;

    @NotNull
    private ReportReason reason;

    private String content;
}
