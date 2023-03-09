package boogi.apiserver.domain.notice.application;

import boogi.apiserver.domain.notice.dao.NoticeRepository;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.notice.dto.dto.CommunityNoticeDetailDto;
import boogi.apiserver.domain.notice.dto.dto.NoticeDetailDto;
import boogi.apiserver.domain.notice.dto.dto.NoticeDto;
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
        return toNoticeDtoList(notices);
    }

    public List<NoticeDetailDto> getAppNotice() {
        List<Notice> notices = noticeRepository.getAllNotices();
        return notices.stream()
                .map(NoticeDetailDto::from)
                .collect(Collectors.toList());
    }

    //todo: iOS와 같이 없애기
    public List<NoticeDto> DEFRECATED_getCommunityLatestNotice(Long communityId) {
        List<Notice> latestNotice = noticeRepository.getLatestNotice(communityId);
        return toNoticeDtoList(latestNotice);
    }

    public List<NoticeDto> getCommunityLatestNotice(Long communityId) {
        return noticeRepository.getLatestNotice(communityId)
                .stream()
                .map(NoticeDto::from)
                .collect(Collectors.toList());
    }

    public List<CommunityNoticeDetailDto> getCommunityNotice(Long communityId) {
        return noticeRepository.getAllNotices(communityId)
                .stream()
                .map(n -> CommunityNoticeDetailDto.of(n, n.getMember().getUser()))
                .collect(Collectors.toList());

    }

    private List<NoticeDto> toNoticeDtoList(List<Notice> notices) {
        return notices.stream()
                .map(NoticeDto::from)
                .collect(Collectors.toList());
    }
}
