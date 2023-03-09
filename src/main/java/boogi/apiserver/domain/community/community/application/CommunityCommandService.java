package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.request.CreateCommunityRequest;
import boogi.apiserver.domain.community.community.exception.AlreadyExistsCommunityNameException;
import boogi.apiserver.domain.community.community.exception.CanNotDeleteCommunityException;
import boogi.apiserver.domain.member.application.MemberCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.dao.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityCommandService {
    private final CommunityRepository communityRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    private final MemberQueryService memberQueryService;

    private final MemberCommandService memberCommandService;

    public Long createCommunity(CreateCommunityRequest request, Long userId) {
        userRepository.findByUserId(userId);
        checkAlreadyExistsName(request.getName());

        Community community = Community.of(request.getName(), request.getDescription(), request.getIsPrivate(),
                request.getAutoApproval(), request.getCategory());

        communityRepository.save(community);
        community.addTags(request.getHashtags());

        memberCommandService.joinMember(userId, community.getId(), MemberType.MANAGER);

        return community.getId();
    }

    public void updateCommunity(Long communityId, String description, List<String> newTags) {
        Community community = communityRepository.findByCommunityId(communityId);

        community.updateCommunity(description, newTags);
    }

    public void shutdown(Long communityId) {
        Community community = communityRepository.findByCommunityId(communityId);

        checkManagerOnlyIncluded(communityId);

        community.shutdown();
    }

    public void changeScope(Long userId, Long communityId, Boolean isSecret) {
        Member member = memberQueryService.getMemberOfTheCommunity(userId, communityId);

        Community community = communityRepository.findByCommunityId(communityId);
        community.switchPrivate(isSecret, member.getMemberType());
    }

    public void changeApproval(Long userId, Long communityId, Boolean isAuto) {
        Member member = memberQueryService.getMemberOfTheCommunity(userId, communityId);

        Community community = communityRepository.findByCommunityId(communityId);
        community.switchAutoApproval(isAuto, member.getMemberType());
    }

    private void checkManagerOnlyIncluded(Long communityId) {
        memberRepository.findAnyMemberExceptManager(communityId).ifPresent(m -> {
            throw new CanNotDeleteCommunityException();
        });
    }

    private void checkAlreadyExistsName(String name) {
        communityRepository.findByCommunityNameEquals(name).ifPresent(c -> {
            throw new AlreadyExistsCommunityNameException();
        });
    }
}
