package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.community.application.CommunityHashtagCoreService;
import boogi.apiserver.domain.member.application.MemberCoreService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityCoreService {
    private final CommunityRepository communityRepository;
    private final MemberRepository memberRepository;

    private final CommunityHashtagCoreService communityHashtagCoreService;
    private final MemberCoreService memberCoreService;

    private final CommunityValidationService communityValidationService;

    private final UserQueryService userQueryService;
    private final CommunityQueryService communityQueryService;

    @Transactional
    public Community createCommunity(Community community, List<String> tags, Long userId) {
        User user = userQueryService.getUser(userId);
        communityValidationService.checkPreviousExistsCommunityName(community.getCommunityName());

        communityRepository.save(community);
        communityHashtagCoreService.addTags(community.getId(), tags);

        memberCoreService.joinMember(userId, community.getId(), MemberType.MANAGER);

        return community;
    }

    @Transactional
    public void shutdown(Long communityId) {
        Community community = communityQueryService.getCommunity(communityId);

        Member member = memberRepository.findAnyMemberExceptManager(communityId);
        if (Objects.nonNull(member)) {
            throw new InvalidValueException("탈퇴하지 않은 부매니저 혹은 일반 맴버가 있습니다.");
        }

        community.shutdown();
    }
}
