package boogi.apiserver.domain.report.api;

import boogi.apiserver.domain.report.application.ReportService;
import boogi.apiserver.domain.report.dto.request.CreateReport;
import boogi.apiserver.global.argument_resolver.session.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/reports")
public class ReportApiController {

    private final ReportService reportService;

    @PostMapping("/")
    public ResponseEntity<Void> createReport(@Validated @RequestBody CreateReport createReport, @Session Long userId) {
        reportService.createReport(createReport, userId);

        return ResponseEntity.ok().build();
    }
}
