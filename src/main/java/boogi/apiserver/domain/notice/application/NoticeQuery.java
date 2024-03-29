package boogi.apiserver.domain.notice.application;

import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.member.application.MemberQuery;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.notice.repository.NoticeRepository;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.notice.dto.dto.NoticeDto;
import boogi.apiserver.domain.notice.dto.response.NoticeDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeQuery {

    private final NoticeRepository noticeRepository;
    private final CommunityRepository communityRepository;

    private final MemberQuery memberQuery;

    public List<NoticeDto> getAppLatestNotice() {
        List<Notice> notices = noticeRepository.getLatestAppNotice();
        return NoticeDto.listFrom(notices);
    }

    public NoticeDetailResponse getAppNotice() {
        List<Notice> notices = noticeRepository.getAllAppNotices();
        return NoticeDetailResponse.from(notices);
    }

    public List<NoticeDto> getCommunityLatestNotice(Long communityId) {
        communityRepository.findCommunityById(communityId);
        List<Notice> latestNotices = noticeRepository.getLatestNotice(communityId);
        return NoticeDto.listFrom(latestNotices);
    }

    public NoticeDetailResponse getCommunityNotice(Long userId, Long communityId) {
        communityRepository.findCommunityById(communityId);
        Member member = memberQuery.getMember(userId, communityId);

        List<Notice> allNotices = noticeRepository.getAllNotices(communityId);
        return NoticeDetailResponse.communityNoticeOf(allNotices, member.isManager());
    }
}
