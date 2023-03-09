package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.AlreadyJoinedMemberException;
import boogi.apiserver.domain.member.exception.NotManagerException;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandService {
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;

    private final MemberQueryService memberQueryService;

    public Member joinMember(Long userId, Long communityId, MemberType type) {
        User user = userRepository.findByUserId(userId);
        Community community = communityRepository.findByCommunityId(communityId);

        checkAlreadyJoinedMember(userId, communityId);

        Member newMember = Member.of(community, user, type);
        memberRepository.save(newMember);
        return newMember;
    }

    public List<Member> joinMembers(List<Long> userIds, Long communityId, MemberType type) {
        checkAlreadyJoinedMembers(userIds, communityId);

        List<User> users = userRepository.findUsersByIds(userIds);
        Community community = communityRepository.findByCommunityId(communityId);

        List<Member> newMembers = users.stream()
                .map(user -> Member.of(community, user, type))
                .collect(Collectors.toList());
        memberRepository.saveAll(newMembers);
        return newMembers;
    }

    public void banMember(Long memberId) {
        Member member = memberRepository.findByMemberId(memberId);
        member.ban();
    }

    public void releaseMember(Long memberId) {
        Member member = memberRepository.findByMemberId(memberId);
        member.release();
    }

    public void delegeteMember(Long userId, Long memberId, MemberType type) {
        Member member = memberRepository.findByMemberId(memberId);

        Member sessionMember = memberQueryService.getMemberOfTheCommunity(userId, member.getCommunity().getId());
        if (!sessionMember.isManager()) {
            throw new NotManagerException();
        }

        if(type.equals(MemberType.MANAGER)) {
            sessionMember.delegate(MemberType.NORMAL);
        }
        member.delegate(type);
    }

    private void checkAlreadyJoinedMember(Long userId, Long communityId) {
        memberRepository.findByUserIdAndCommunityId(userId, communityId).ifPresent(m -> {
            throw new AlreadyJoinedMemberException();
        });
    }

    private void checkAlreadyJoinedMembers(List<Long> userIds, Long communityId) {
        List<Member> joinedMembers = memberRepository.findAlreadyJoinedMemberByUserId(userIds, communityId);

        boolean hasJoinedMember = joinedMembers.stream()
                .anyMatch(m -> userIds.contains(m.getUser().getId()));
        if (hasJoinedMember) {
            throw new AlreadyJoinedMemberException();
        }
    }
}
