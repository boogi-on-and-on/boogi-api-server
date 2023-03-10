package boogi.apiserver.domain.community.joinrequest.application;

import boogi.apiserver.domain.community.community.dto.dto.UserJoinRequestInfoDto;
import boogi.apiserver.domain.community.joinrequest.dao.JoinRequestRepository;
import boogi.apiserver.domain.member.application.MemberQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JoinRequestQueryService {
    private final JoinRequestRepository joinRequestRepository;

    private final MemberQueryService memberQueryService;

    public List<UserJoinRequestInfoDto> getAllRequests(Long userId, Long communityId) {
        memberQueryService.getOperator(userId, communityId);

        return joinRequestRepository.getAllRequests(communityId)
                .stream()
                .map(r -> UserJoinRequestInfoDto.of(r.getUser(), r.getId()))
                .collect(Collectors.toList());
    }
}
