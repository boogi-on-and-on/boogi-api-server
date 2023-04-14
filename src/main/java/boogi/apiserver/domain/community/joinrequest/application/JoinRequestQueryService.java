package boogi.apiserver.domain.community.joinrequest.application;

import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.community.community.dto.dto.UserJoinRequestInfoDto;
import boogi.apiserver.domain.community.joinrequest.repository.JoinRequestRepository;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.member.application.MemberQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JoinRequestQueryService {
    private final JoinRequestRepository joinRequestRepository;

    private final MemberQueryService memberQueryService;
    private final CommunityRepository communityRepository;

    public List<UserJoinRequestInfoDto> getAllPendingRequests(Long userId, Long communityId) {
        communityRepository.findCommunityById(communityId);
        memberQueryService.getOperator(userId, communityId);

        List<JoinRequest> joinRequests = joinRequestRepository.getAllPendingRequests(communityId);
        return UserJoinRequestInfoDto.listOf(joinRequests);
    }
}
