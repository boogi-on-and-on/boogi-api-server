package boogi.apiserver.domain.notice.api;

import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.dto.NoticeDetailDto;
import boogi.apiserver.domain.notice.dto.NoticeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/notices")
public class NoticeApiController {

    private final NoticeQueryService noticeQueryService;

    @GetMapping
    public ResponseEntity<Object> getLatestNotice(@RequestParam(required = false) Long communityId) {
        List<NoticeDto> latestNotice;
        if (communityId == null) {
            latestNotice = noticeQueryService.getAppLatestNotice();
        } else {
            latestNotice = noticeQueryService.getCommunityLatestNotice(communityId);
        }

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "notices", latestNotice
        ));
    }

    //todo: 커뮤니티 공지사항 목록 구현
    @GetMapping("/list")
    public ResponseEntity<Object> getAllNotice(@RequestParam(required = false) Long communityId) {
        List<NoticeDetailDto> appAllNotice = noticeQueryService.getAppNotice();

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "notices", appAllNotice
        ));
    }
}
