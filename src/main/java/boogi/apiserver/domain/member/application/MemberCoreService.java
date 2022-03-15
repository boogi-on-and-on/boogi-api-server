package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.community.community.application.CommunityValidationService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.application.UserValidationService;
import boogi.apiserver.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberCoreService {

    private final MemberRepository memberRepository;

    private final CommunityValidationService communityValidationService;
    private final MemberValidationService memberValidationService;
    private final UserValidationService userValidationService;


    @Transactional
    public Member joinMember(Long userId, Long communityId, MemberType type) {
        User user = userValidationService.getUser(userId);
        Community community = communityValidationService.getCommunity(communityId);

        memberValidationService.checkAlreadyJoinedMember(userId, communityId);

        Member member = Member.createNewMember(community, user, type);
        memberRepository.save(member);

        return member;
    }
}
