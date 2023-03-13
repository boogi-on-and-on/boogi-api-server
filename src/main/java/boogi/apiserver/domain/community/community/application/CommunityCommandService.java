package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.request.CommunitySettingRequest;
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
        validateAlreadyExistName(request.getName());

        Community community = Community.of(
                request.getName(), request.getDescription(), request.getIsPrivate(),
                request.getAutoApproval(), request.getCategory());

        communityRepository.save(community);
        community.addTags(request.getHashtags());

        Long newCommunityId = community.getId();
        memberCommandService.joinMember(userId, newCommunityId, MemberType.MANAGER);

        return newCommunityId;
    }

    public void updateCommunity(Long userId, Long communityId, String description, List<String> newTags) {
        Community community = communityRepository.findByCommunityId(communityId);
        memberQueryService.getManager(userId, communityId);

        community.updateCommunity(description, newTags);
    }

    public void shutdown(Long userId, Long communityId) {
        Community community = communityRepository.findByCommunityId(communityId);
        memberQueryService.getManager(userId, communityId);

        validateManagerOnlyIncluded(communityId);

        community.shutdown();
    }

    public void changeSetting(Long userId, Long communityId, CommunitySettingRequest request) {
        Member member = memberQueryService.getMember(userId, communityId);
        Community community = communityRepository.findByCommunityId(communityId);

        Boolean isAuto = request.getIsAutoApproval();
        Boolean isSecret = request.getIsSecret();
        MemberType memberType = member.getMemberType();
        if (isAuto != null) {
            community.switchAutoApproval(isAuto, memberType);
        }
        if (isSecret != null) {
            community.switchPrivate(isSecret, memberType);
        }
    }

    private void validateManagerOnlyIncluded(Long communityId) {
        memberRepository.findAnyMemberExceptManager(communityId).ifPresent(m -> {
            throw new CanNotDeleteCommunityException();
        });
    }

    private void validateAlreadyExistName(String name) {
        communityRepository.findByCommunityNameEquals(name).ifPresent(c -> {
            throw new AlreadyExistsCommunityNameException();
        });
    }
}
