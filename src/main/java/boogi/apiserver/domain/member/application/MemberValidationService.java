package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.AlreadyJoinedMemberException;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberValidationService {

    private final MemberRepository memberRepository;

    public Member checkAlreadyJoinedMember(Long userId, Long communityId) {
        List<Member> members = memberRepository.findByUserIdAndCommunityId(userId, communityId);
        if (members.size() > 0) {
            throw new AlreadyJoinedMemberException();
        }
        return null;
    }

    public boolean hasSupervisorAuth(Long userId, Long communityId) {
        List<Member> members = memberRepository.findByUserIdAndCommunityId(userId, communityId);
        if (members.size() == 0) {
            throw new InvalidValueException("가입하지 않은 멤버입니다.");
        }
        Member member = members.get(0);

        boolean isSupervisor = Arrays.asList(MemberType.MANAGER, MemberType.SUB_MANAGER)
                .stream()
                .anyMatch(type -> member.getMemberType().equals(type));

        if (!isSupervisor) {
            throw new NotAuthorizedMemberException();
        }
        return true;
    }
}
