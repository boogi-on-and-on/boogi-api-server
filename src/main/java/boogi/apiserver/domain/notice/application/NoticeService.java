package boogi.apiserver.domain.notice.application;

import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.notice.dao.NoticeRepository;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final CommunityRepository communityRepository;

    private final MemberQueryService memberQueryService;

    @Transactional
    public Notice create(Map<String, String> request, Long userId, Long communityId) {
        Member member = memberQueryService.getMemberOfTheCommunity(userId, communityId);
        boolean isOperator = List.of(MemberType.MANAGER, MemberType.SUB_MANAGER)
                .contains(member.getMemberType());

        if (!isOperator) {
            throw new InvalidValueException("관리자가 아닙니다.");
        }

        Community community = communityRepository.findByCommunityId(communityId);

        String content = request.get("content");
        String title = request.get("title");

        Notice notice = Notice.of(content, title, member, community);

        noticeRepository.save(notice);
        return notice;
    }
}
