package boogi.apiserver.domain.report.dto.request;

import boogi.apiserver.domain.report.domain.ReportReason;
import boogi.apiserver.domain.report.domain.ReportTarget;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReport {

    @NotNull(message = "신고할 대상을 입력해주세요")
    private Long id;

    @NotNull(message = "신고 대상의 종류를 입력해주세요")
    private ReportTarget target;

    @NotNull(message = "신고 사유를 입력해주세요")
    private ReportReason reason;

    private String content;
}
