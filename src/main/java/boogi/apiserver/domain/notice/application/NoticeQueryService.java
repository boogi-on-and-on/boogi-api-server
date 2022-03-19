package boogi.apiserver.domain.notice.application;

import boogi.apiserver.domain.notice.dao.NoticeRepository;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.notice.dto.NoticeDetailDto;
import boogi.apiserver.domain.notice.dto.NoticeDto;
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

    public List<NoticeDto> getCommunityLatestNotice(Long communityId) {
        List<Notice> latestNotice = noticeRepository.getLatestNotice(communityId);
        return transformToLatestNotice(latestNotice);
    }

    //todo: 커뮤니티 공지사항 리스트 구현

    private List<NoticeDto> transformToLatestNotice(List<Notice> notices) {
        return notices.stream()
                .map(NoticeDto::of)
                .collect(Collectors.toList());
    }
}
