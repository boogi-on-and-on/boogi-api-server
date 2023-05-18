package boogi.apiserver.domain.community.joinrequest.application;

import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.joinrequest.repository.JoinRequestRepository;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequestStatus;
import boogi.apiserver.domain.community.joinrequest.exception.AlreadyRequestedException;
import boogi.apiserver.domain.member.application.MemberCommand;
import boogi.apiserver.domain.member.application.MemberQuery;
import boogi.apiserver.domain.member.repository.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.AlreadyJoinedMemberException;
import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class JoinRequestCommand {

    private final JoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final MemberRepository memberRepository;

    private final MemberCommand memberCommand;

    private final MemberQuery memberQuery;


    //todo: 거절했는데, 계속 요청하면 어떻게 할지? --> 커뮤니티에서 유저(멤버x)차단 기능 필요?
    public Long request(Long userId, Long communityId) {
        User user = userRepository.findUserById(userId);
        Community community = communityRepository.findCommunityById(communityId);

        validateAlreadyJoinedMember(userId, community);
        validateJoinRequestStatus(userId, communityId);

        JoinRequest newRequest = JoinRequest.of(user, community);

        if (community.isAutoApproval()) {
            Member member = Member.of(community, user, MemberType.NORMAL);
            memberRepository.save(member);

            Member manager = memberRepository.findManager(communityId);
            newRequest.confirm(manager, member);
        }

        joinRequestRepository.save(newRequest);

        return newRequest.getId();
    }

    public void confirmUsers(Long sessionUserId, List<Long> requestIds, Long communityId) {
        communityRepository.findCommunityById(communityId);
        Member operator = memberQuery.getOperator(sessionUserId, communityId);
        List<JoinRequest> joinRequests = joinRequestRepository.getRequestsByIds(requestIds);
        joinRequests.forEach(joinRequest -> joinRequest.validateJoinRequestCommunity(communityId));

        List<Long> userIds = joinRequests.stream()
                .map(r -> r.getUser().getId())
                .collect(Collectors.toList());

        List<Member> members = memberCommand.joinMembers(userIds, communityId, MemberType.NORMAL);
        confirmRequests(operator, joinRequests, members);
    }

    public void rejectUsers(Long userId, List<Long> requestIds, Long communityId) {
        communityRepository.findCommunityById(communityId);
        Member operator = memberQuery.getOperator(userId, communityId);
        rejectRequests(requestIds, communityId, operator);
    }

    private void validateJoinRequestStatus(Long userId, Long communityId) {
        joinRequestRepository.getLatestJoinRequest(userId, communityId)
                .map(JoinRequest::getStatus)
                .ifPresent(this::validateInvalidJoinRequestStatus);
    }

    private void validateInvalidJoinRequestStatus(JoinRequestStatus status) {
        switch (status) {
            case PENDING:
                throw new AlreadyRequestedException();
        }
    }

    private void validateAlreadyJoinedMember(Long userId, Community community) {
        Member member = memberQuery.getMemberOrNullMember(userId, community);
        if (!member.isNullMember()) {
            throw new AlreadyJoinedMemberException();
        }
    }

    private void confirmRequests(Member operator,
                                 List<JoinRequest> joinRequests,
                                 List<Member> members) {
        Map<Long, Member> memberMap = members.stream()
                .collect(Collectors.toMap(m -> m.getUser().getId(), m -> m));

        joinRequests.forEach(r -> r.confirm(operator, memberMap.get(r.getUser().getId())));
    }

    private void rejectRequests(List<Long> requestIds, Long communityId, Member operator) {
        joinRequestRepository.getRequestsByIds(requestIds)
                .forEach(joinRequest -> {
                    joinRequest.validateJoinRequestCommunity(communityId);
                    joinRequest.reject(operator);
                });
    }
}
