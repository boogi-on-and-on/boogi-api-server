package boogi.apiserver.domain.report.api;

import boogi.apiserver.domain.report.dto.CreateReport;
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

    @PostMapping("/")
    public ResponseEntity<Object> createReport(@Validated @RequestBody CreateReport createReport, @Session Long userId){
        throw new RuntimeException("구현해야함");

        return ResponseEntity.ok().build();
    }
}
