package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommand {
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;

    private final MemberQuery memberQuery;

    public Member joinMember(Long userId, Long communityId, MemberType type) {
        User user = userRepository.findUserById(userId);
        Community community = communityRepository.findCommunityById(communityId);

        validateAlreadyJoinedMember(userId, communityId);

        Member newMember = Member.of(community, user, type);
        memberRepository.save(newMember);
        return newMember;
    }

    public List<Member> joinMembers(List<Long> userIds, Long communityId, MemberType type) {
        List<User> users = userRepository.findUsersByIds(userIds);
        Community community = communityRepository.findCommunityById(communityId);

        validateAlreadyJoinedMember(userIds, communityId);

        List<Member> newMembers = users.stream()
                .map(user -> Member.of(community, user, type))
                .collect(Collectors.toList());
        memberRepository.saveAll(newMembers);
        return newMembers;
    }

    public void banMember(Long sessionUserId, Long memberId) {
        Member member = memberRepository.findMemberById(memberId);
        memberQuery.getOperator(sessionUserId, member.getCommunity().getId());

        member.ban();
    }

    public void releaseMember(Long sessionUserId, Long memberId) {
        Member member = memberRepository.findMemberById(memberId);
        memberQuery.getManager(sessionUserId, member.getCommunity().getId());

        member.release();
    }

    public void delegateMember(Long userId, Long memberId, MemberType type) {
        Member member = memberRepository.findMemberById(memberId);

        Member manager = memberQuery.getManager(userId, member.getCommunity().getId());
        if (MemberType.MANAGER.equals(type)) {
            manager.changeMemberType(MemberType.NORMAL);
        }
        member.changeMemberType(type);
    }

    private void validateAlreadyJoinedMember(Long userId, Long communityId) {
        validateAlreadyJoinedMember(List.of(userId), communityId);
    }

    private void validateAlreadyJoinedMember(List<Long> userIds, Long communityId) {
        List<Member> joinedMembers = memberRepository.findAlreadyJoinedMember(userIds, communityId);

        boolean hasJoinedMember = joinedMembers.stream()
                .anyMatch(m -> userIds.contains(m.getUser().getId()));
        if (hasJoinedMember) {
            throw new AlreadyJoinedMemberException();
        }
    }
}
