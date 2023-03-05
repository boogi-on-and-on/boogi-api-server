package boogi.apiserver.domain.community.joinrequest.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.joinrequest.dao.JoinRequestRepository;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequestStatus;
import boogi.apiserver.domain.member.application.MemberCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class JoinRequestCommandService {

    private final JoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;

    private final MemberCommandService memberCommandService;

    private final JoinRequestQueryService joinRequestQueryService;
    private final MemberQueryService memberQueryService;

    private final MemberRepository memberRepository;

    //todo: 거절했는데, 계속 요청하면 어떻게 할지? --> 커뮤니티에서 유저(멤버x)차단 기능 필요?
    public Long request(Long userId, Long communityId) {
        Member alreadyJoinedMember = memberQueryService.getMemberOfTheCommunity(userId, communityId);
        if (Objects.nonNull(alreadyJoinedMember)) {
            throw new InvalidValueException("이미 가입한 커뮤니티입니다.");
        }

        User user = userRepository.findByUserId(userId);
        Community community = communityRepository.findByCommunityId(communityId);

        Optional<JoinRequest> possibleLatestRequest = joinRequestRepository.getLatestJoinRequest(userId, communityId);
        if (possibleLatestRequest.isPresent()) {
            JoinRequest latestRequest = possibleLatestRequest.get();
            switch (latestRequest.getStatus()) {
                case CONFIRM:
                    throw new InvalidValueException("이미 가입한 커뮤니티입니다.");
                case PENDING:
                    throw new InvalidValueException("이미 요청한 커뮤니티입니다.");
            }
        }
        JoinRequest request = JoinRequest.of(user, community);

        if (community.isAutoApproval()) {
            Member member = Member.of(community, user, MemberType.NORMAL);
            memberRepository.save(member);

            Member manager = memberRepository.findManager(communityId);
            request.confirm(manager, member);
        }

        joinRequestRepository.save(request);

        return request.getId();
    }

    public void confirmUser(Long managerUserId, Long requestId, Long communityId) {
        JoinRequest joinRequest = joinRequestRepository.findByJoinRequestId(requestId);
        isValidJoinRequestEntity(joinRequest, communityId);

        Member manager = memberQueryService.getMemberOfTheCommunity(managerUserId, communityId);
        isOperator(manager);

        Long userId = joinRequest.getUser().getId();
        User user = userRepository.findByUserId(userId);

        Member newMember = memberCommandService.joinMember(userId, communityId, MemberType.NORMAL);

        joinRequest.confirm(manager, newMember);
    }

    private void isOperator(Member manager) {
        if (Objects.isNull(manager)) {
            throw new InvalidValueException("운영자의 계정을 확인해주세요.");
        }
    }

    public void confirmUserInBatch(Long managerUserId, List<Long> requestIds, Long communityId) {
        List<JoinRequest> joinRequests = joinRequestRepository.getRequestsByIds(requestIds);
        joinRequests.forEach(joinRequest -> {
            isValidJoinRequestEntity(joinRequest, communityId);
            isPendingJoinRequest(joinRequest);
        });
        List<Long> userIds = joinRequests.stream()
                .map(r -> r.getUser().getId())
                .collect(Collectors.toList());

        List<Member> members = memberCommandService.joinMemberInBatch(userIds, communityId, MemberType.NORMAL);
        Map<Long, Member> memberMap = members.stream()
                .collect(Collectors.toMap(m -> m.getUser().getId(), m -> m));

        Member manager = memberQueryService.getMemberOfTheCommunity(managerUserId, communityId);
        joinRequests
                .forEach(r -> r.confirm(manager, memberMap.get(r.getUser().getId())));
    }

    public void rejectUserInBatch(Long managerUserId, List<Long> requestIds, Long communityId) {
        Member manager = memberQueryService.getMemberOfTheCommunity(managerUserId, communityId);
        isOperator(manager);

        joinRequestRepository.getRequestsByIds(requestIds)
                .forEach(joinRequest -> {
                    isValidJoinRequestEntity(joinRequest, communityId);
                    isPendingJoinRequest(joinRequest);

                    joinRequest.reject(manager);
                });
    }

    private void isPendingJoinRequest(JoinRequest joinRequest) {
        if (!joinRequest.getStatus().equals(JoinRequestStatus.PENDING)) {
            throw new InvalidValueException("승인 대기중인 요청이 아닙니다.");
        }
    }

    private void isValidJoinRequestEntity(JoinRequest joinRequest, Long communityId) {
        if (!joinRequest.getCommunity().getId().equals(communityId)) {
            throw new InvalidValueException("해당 커뮤니티에서 처리할 수 없는 요청입니다.");
        }
    }
}
