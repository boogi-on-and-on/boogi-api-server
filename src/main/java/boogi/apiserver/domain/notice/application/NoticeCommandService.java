package boogi.apiserver.domain.notice.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.notice.dao.NoticeRepository;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.notice.dto.request.NoticeCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeCommandService {
    private final NoticeRepository noticeRepository;
    private final CommunityRepository communityRepository;

    private final MemberQueryService memberQueryService;

    public Long createNotice(NoticeCreateRequest request, Long userId) {
        Long communityId = request.getCommunityId();
        Community community = communityRepository.findCommunityById(communityId);

        Member member = memberQueryService.getOperator(userId, communityId);

        Notice newNotice = Notice.of(request.getContent(), request.getTitle(), member, community);
        noticeRepository.save(newNotice);
        return newNotice.getId();
    }
}
