package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.community.application.CommunityHashtagCoreService;
import boogi.apiserver.domain.member.application.MemberCoreService;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.application.UserValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityCoreService {

    private final CommunityRepository communityRepository;

    private final CommunityHashtagCoreService communityHashtagCoreService;
    private final MemberCoreService memberCoreService;

    private final UserValidationService userValidationService;
    private final CommunityValidationService communityValidationService;

    @Transactional
    public Community createCommunity(Community community, List<String> tags, Long userId) {
        userValidationService.getUser(userId);
        communityValidationService.checkPreviousExistsCommunityName(community.getCommunityName());

        communityRepository.save(community);
        communityHashtagCoreService.addTags(community.getId(), tags);

        memberCoreService.joinMember(userId, community.getId(), MemberType.MANAGER);

        return community;
    }
}
