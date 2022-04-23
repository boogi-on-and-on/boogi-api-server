package boogi.apiserver.domain.report.application;


import boogi.apiserver.domain.report.dao.ReportRepository;
import boogi.apiserver.domain.report.dto.CreateReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    @Transactional
    public void createReport(CreateReport createReport, Long userId) {
        // target에 해당하는 object가 존재하는지 확인

        //
    }
}
