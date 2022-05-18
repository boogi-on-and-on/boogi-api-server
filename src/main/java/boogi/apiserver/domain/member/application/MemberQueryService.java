package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.dto.BannedMemberDto;
import boogi.apiserver.domain.user.dto.UserJoinedCommunity;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberRepository memberRepository;

    public Member getMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(InvalidValueException::new);
        if (Objects.nonNull(member.getCanceledAt())) {
            throw new EntityNotFoundException();
        }
        return member;
    }

    public List<UserJoinedCommunity> getJoinedMemberInfo(Long userId) {
        return memberRepository.findByUserId(userId).stream()
                .map(m -> UserJoinedCommunity.of(m.getCommunity()))
                .collect(Collectors.toList());
    }

    public Member getMemberOfTheCommunity(Long userId, Long communityId) {
        List<Member> members = memberRepository.findByUserIdAndCommunityId(userId, communityId);

        return members.size() >= 1 ? members.get(0) : null;
    }

    public Boolean hasAuth(Long userId, Long communityId, MemberType memberType) {
        Member member = this.getMemberOfTheCommunity(userId, communityId);
        if (Objects.isNull(member)) {
            return false;
        }
        return member.getMemberType().equals(memberType);
    }

    public Page<Member> getCommunityJoinedMembers(Pageable pageable, Long communityId) {
        return memberRepository.findJoinedMembers(pageable, communityId);
    }

    public List<BannedMemberDto> getBannedMembers(Long communityId) {
        return memberRepository.findBannedMembers(communityId);
    }
}
