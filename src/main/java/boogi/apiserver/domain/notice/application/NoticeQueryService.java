package boogi.apiserver.domain.notice.application;

import boogi.apiserver.domain.notice.dao.NoticeRepository;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.notice.dto.response.CommunityNoticeDetailDto;
import boogi.apiserver.domain.notice.dto.response.NoticeDetailDto;
import boogi.apiserver.domain.notice.dto.response.NoticeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeQueryService {

    private final NoticeRepository noticeRepository;

    public List<NoticeDto> getAppLatestNotice() {
        List<Notice> notices = noticeRepository.getLatestNotice();
        return transformToLatestNotice(notices);
    }

    public List<NoticeDetailDto> getAppNotice() {
        List<Notice> notices = noticeRepository.getAllNotices();
        return notices.stream()
                .map(NoticeDetailDto::of)
                .collect(Collectors.toList());
    }

    public List<NoticeDto> DEFRECATED_getCommunityLatestNotice(Long communityId) {
        List<Notice> latestNotice = noticeRepository.getLatestNotice(communityId);
        return transformToLatestNotice(latestNotice);
    }

    public List<NoticeDto> getCommunityLatestNotice(Long communityId) {
        return noticeRepository.getLatestNotice(communityId)
                .stream()
                .map(NoticeDto::of)
                .collect(Collectors.toList());
    }

    public List<CommunityNoticeDetailDto> getCommunityNotice(Long communityId) {
        return noticeRepository.getAllNotices(communityId)
                .stream()
                .map(n -> CommunityNoticeDetailDto.of(n, n.getMember().getUser()))
                .collect(Collectors.toList());

    }

    private List<NoticeDto> transformToLatestNotice(List<Notice> notices) {
        return notices.stream()
                .map(NoticeDto::of)
                .collect(Collectors.toList());
    }
}
