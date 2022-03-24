package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberCoreService {
    private final MemberRepository memberRepository;

    private final MemberValidationService memberValidationService;

    private final MemberQueryService memberQueryService;
    private final CommunityQueryService communityQueryService;
    private final UserQueryService userQueryService;

    @Transactional
    public Member joinMember(Long userId, Long communityId, MemberType type) {
        User user = userQueryService.getUser(userId);
        Community community = communityQueryService.getCommunity(communityId);

        memberValidationService.checkAlreadyJoinedMember(userId, communityId);

        Member member = Member.createNewMember(community, user, type);
        memberRepository.save(member);

        return member;
    }

    @Transactional
    public void banMember(Long memberId) {
        Member member = memberQueryService.getMember(memberId);

        if (Objects.nonNull(member.getBannedAt())) {
            throw new InvalidValueException("이미 차단된 멤버입니다.");
        }

        member.ban();
    }
}
