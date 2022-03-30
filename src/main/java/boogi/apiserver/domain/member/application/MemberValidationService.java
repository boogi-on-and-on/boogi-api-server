package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.AlreadyJoinedMemberException;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

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

    public Member checkMemberJoinedCommunity(Long userId, Long communityId) {
        List<Member> members = memberRepository.findByUserIdAndCommunityId(userId, communityId);
        if (members.isEmpty()) {
            throw new NotJoinedMemberException();
        }
        return members.get(0);
    }

    public boolean hasAuth(Long userId, Long communityId, MemberType memberType) {
        List<Member> members = memberRepository.findByUserIdAndCommunityId(userId, communityId);
        if (members.size() == 0) {
            throw new InvalidValueException("가입하지 않은 멤버입니다.");
        }
        Member member = members.get(0);

        Boolean hasAuth;
        switch (memberType) {
            case MANAGER:
                hasAuth = member.getMemberType().equals(MemberType.MANAGER);
                break;
            case SUB_MANAGER:
                hasAuth = Stream.of(MemberType.MANAGER, MemberType.SUB_MANAGER)
                        .anyMatch(type -> member.getMemberType().equals(type));
                break;
            case NORMAL:
            default:
                hasAuth = false;
                break;
        }

        if (!hasAuth) {
            throw new NotAuthorizedMemberException();
        }
        return true;
    }
}
