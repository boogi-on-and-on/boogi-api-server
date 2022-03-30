package boogi.apiserver.domain.notice.api;

import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.dto.CommunityNoticeDetailDto;
import boogi.apiserver.domain.notice.dto.NoticeDetailDto;
import boogi.apiserver.domain.notice.dto.NoticeDto;
import boogi.apiserver.global.argument_resolver.session.Session;
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
    private final MemberQueryService memberQueryService;

    @GetMapping
    public ResponseEntity<Object> getAllNotice(@RequestParam(required = false) Long communityId,
                                               @Session Long userId
    ) {
        if (communityId == null) {
            List<NoticeDetailDto> appAllNotice = noticeQueryService.getAppNotice();
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "notices", appAllNotice
            ));
        }

        List<CommunityNoticeDetailDto> communityAllNotice = noticeQueryService.getCommunityNotice(communityId);
        Boolean hasManagerAuth = memberQueryService.hasAuth(userId, communityId, MemberType.MANAGER);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "notices", communityAllNotice,
                "manager", hasManagerAuth
        ));
    }

    @GetMapping("/recent")
    public ResponseEntity<Object> getLatestNotice(@RequestParam(required = false) Long communityId) {
        List<NoticeDto> latestNotice;
        if (communityId == null) {
            latestNotice = noticeQueryService.getAppLatestNotice();
        } else {
            latestNotice = noticeQueryService.DEFRECATED_getCommunityLatestNotice(communityId);
        }

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "notices", latestNotice
        ));
    }
}
