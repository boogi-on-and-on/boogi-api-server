package boogi.apiserver.domain.notice.api;

import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.application.NoticeCommandService;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.notice.dto.dto.CommunityNoticeDetailDto;
import boogi.apiserver.domain.notice.dto.dto.NoticeDto;
import boogi.apiserver.domain.notice.dto.request.NoticeCreateRequest;
import boogi.apiserver.domain.notice.dto.response.LatestNoticeResponse;
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

    private final NoticeQueryService noticeQueryService;
    private final MemberQueryService memberQueryService;

    private final NoticeCommandService noticeCommandService;

    private final MemberValidationService memberValidationService;

    @GetMapping
    public NoticeDetailResponse getAllNotice(@RequestParam(required = false) Long communityId,
                                             @Session Long userId
    ) {
        if (communityId == null) {
            return NoticeDetailResponse.of(noticeQueryService.getAppNotice());
        }

        List<CommunityNoticeDetailDto> communityAllNotice = noticeQueryService.getCommunityNotice(communityId);
        Boolean hasManagerAuth = memberQueryService.hasAuth(userId, communityId, MemberType.MANAGER);
        return NoticeDetailResponse.communityNoticeOf(communityAllNotice, hasManagerAuth);
    }

    @PostMapping
    public SimpleIdResponse createNotice(@RequestBody @Validated NoticeCreateRequest request, @Session Long userId) {
        memberValidationService.hasAuth(userId, request.getCommunityId(), MemberType.SUB_MANAGER);

        Notice newNotice = noticeCommandService.createNotice(request, userId);

        return SimpleIdResponse.from(newNotice.getId());
    }

    @GetMapping("/recent")
    public LatestNoticeResponse getLatestNotice(@RequestParam(required = false) Long communityId) {
        List<NoticeDto> latestNotices = (communityId == null) ? noticeQueryService.getAppLatestNotice()
                : noticeQueryService.DEFRECATED_getCommunityLatestNotice(communityId);

        return LatestNoticeResponse.from(latestNotices);
    }
}
