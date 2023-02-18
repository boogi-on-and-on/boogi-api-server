package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.NotViewableMemberException;
import boogi.apiserver.domain.member.vo.NullMember;
import boogi.apiserver.domain.member.dto.dto.BannedMemberDto;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.domain.user.dto.dto.UserJoinedCommunityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberRepository memberRepository;

    public Member findByMemberId(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(EntityNotFoundException::new);
    }

    public Member getJoinedMember(Long userId, Long communityId) {
        return memberRepository.findByUserIdAndCommunityId(userId, communityId)
                .orElseThrow(NotJoinedMemberException::new);
    }

    public Member getViewableMember(Long userId, Community community) {
        Member member = memberRepository.findByUserIdAndCommunityId(userId, community.getId())
                .orElse(new NullMember());

        if (community.isPrivate() && !member.isJoined()) {
            throw new NotViewableMemberException();
        }
        return member;
    }

    public List<UserJoinedCommunityDto> getJoinedMemberInfo(Long userId) {
        return memberRepository.findByUserId(userId).stream()
                .map(m -> UserJoinedCommunityDto.of(m.getCommunity()))
                .collect(Collectors.toList());
    }

    public Member getMemberOfTheCommunity(Long userId, Long communityId) {
        return memberRepository.findByUserIdAndCommunityId(userId, communityId)
                .orElse(null);
    }

    public Boolean hasAuth(Long userId, Long communityId, MemberType memberType) {
        Member member = this.getMemberOfTheCommunity(userId, communityId);
        if (Objects.isNull(member)) {
            return false;
        }
        return member.getMemberType().equals(memberType);
    }

    public Slice<Member> getCommunityJoinedMembers(Pageable pageable, Long communityId) {
        return memberRepository.findJoinedMembers(pageable, communityId);
    }

    public List<BannedMemberDto> getBannedMembers(Long communityId) {
        return memberRepository.findBannedMembers(communityId);
    }

    public Slice<UserBasicProfileDto> getMentionSearchMembers(Pageable pageable, Long communityId, String name) {
        return memberRepository.findMentionMember(pageable, communityId, name);
    }
}
