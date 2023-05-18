package boogi.apiserver.domain.notice.controller;

import boogi.apiserver.domain.notice.application.NoticeCommand;
import boogi.apiserver.domain.notice.application.NoticeQuery;
import boogi.apiserver.domain.notice.dto.dto.NoticeDto;
import boogi.apiserver.domain.notice.dto.request.NoticeCreateRequest;
import boogi.apiserver.domain.notice.dto.response.LatestAppNoticeResponse;
import boogi.apiserver.domain.notice.dto.response.NoticeDetailResponse;
import boogi.apiserver.global.argument_resolver.session.Session;
import boogi.apiserver.global.dto.SimpleIdResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/notices")
public class NoticeApiController {

    private final NoticeQuery noticeQuery;

    private final NoticeCommand noticeCommand;

    @GetMapping
    public NoticeDetailResponse getAllNotice(@RequestParam(required = false) Long communityId,
                                             @Session Long userId) {
        if (communityId == null) {
            return noticeQuery.getAppNotice();
        }
        return noticeQuery.getCommunityNotice(userId, communityId);
    }

    @PostMapping
    public SimpleIdResponse createNotice(@RequestBody @Validated NoticeCreateRequest request,
                                         @Session Long userId) {
        Long newNoticeId = noticeCommand.createNotice(request, userId);

        return SimpleIdResponse.from(newNoticeId);
    }

    @GetMapping("/recent")
    public LatestAppNoticeResponse getLatestAppNotices() {
        List<NoticeDto> latestAppNotices = noticeQuery.getAppLatestNotice();

        return LatestAppNoticeResponse.from(latestAppNotices);
    }
}
