package boogi.apiserver.domain.community.joinrequest.application;

import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.joinrequest.dao.JoinRequestRepository;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.member.application.MemberCoreService;
import boogi.apiserver.domain.member.application.MemberQueryService;
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
public class JoinRequestCoreService {

    private final JoinRequestRepository joinRequestRepository;

    private final MemberCoreService memberCoreService;

    private final JoinRequestQueryService joinRequestQueryService;
    private final UserQueryService userQueryService;
    private final CommunityQueryService communityQueryService;
    private final MemberQueryService memberQueryService;

    //todo: 거절했는데, 계속 요청하면 어떻게 할지? --> 커뮤니티에서 유저(멤버x)차단 기능 필요?
    @Transactional
    public Long request(Long userId, Long communityId) {
        Member alreadyJoinedMember = memberQueryService.getMemberOfTheCommunity(userId, communityId);
        if (Objects.nonNull(alreadyJoinedMember)) {
            throw new InvalidValueException("이미 가입한 커뮤니티입니다.");
        }

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

    @Transactional
    public void confirmUser(Long managerUserId, Long requestId, Long communityId) {
        JoinRequest joinRequest = joinRequestQueryService.getJoinRequest(requestId);
        isValidJoinRequestEntity(joinRequest, communityId);

        Member manager = memberQueryService.getMemberOfTheCommunity(managerUserId, communityId);

        Long userId = joinRequest.getUser().getId();
        User user = userQueryService.getUser(userId);

        Member newMember = memberCoreService.joinMember(userId, communityId, MemberType.NORMAL);

        joinRequest.confirm(manager, user, newMember);
    }

    @Transactional
    public void rejectUser(Long managerUserId, Long requestId, Long communityId) {
        JoinRequest joinRequest = joinRequestQueryService.getJoinRequest(requestId);
        isValidJoinRequestEntity(joinRequest, communityId);

        Member manager = memberQueryService.getMemberOfTheCommunity(managerUserId, communityId);

        Long userId = joinRequest.getUser().getId();
        User user = userQueryService.getUser(userId);

        joinRequest.reject(manager, user);
    }

    private void isValidJoinRequestEntity(JoinRequest joinRequest, Long communityId) {
        if (!joinRequest.getCommunity().getId().equals(communityId)) {
            throw new InvalidValueException();
        }
    }
}
