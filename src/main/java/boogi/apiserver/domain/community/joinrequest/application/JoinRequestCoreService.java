package boogi.apiserver.domain.community.joinrequest.application;

import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.joinrequest.dao.JoinRequestRepository;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequestStatus;
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
public class JoinRequestCoreService {

    private final JoinRequestRepository joinRequestRepository;

    private final JoinRequestQueryService joinRequestQueryService;
    private final UserQueryService userQueryService;
    private final CommunityQueryService communityQueryService;

    //todo: 거절했는데, 계속 요청하면 어떻게 할지? --> 커뮤니티에서 유저(멤버x)차단 기능 필요?
    @Transactional
    public Long request(Long userId, Long communityId) {
        User user = userQueryService.getUser(userId);
        Community community = communityQueryService.getCommunity(communityId);

        JoinRequest latestRequest = joinRequestRepository.getLatestJoinRequest(userId, communityId);
        if (Objects.nonNull(latestRequest)) {
            switch (latestRequest.getStatus()) {
                case CONFIRM:
                    throw new InvalidValueException("이미 가입한 커뮤니티입니다.");
                case PENDING:
                    throw new InvalidValueException("이미 요청한 커뮤니티입니다.");
            }
        }
        JoinRequest request = JoinRequest.of(user, community);
        joinRequestRepository.save(request);

        return request.getId();
    }
}
