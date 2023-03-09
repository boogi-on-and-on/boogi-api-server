package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.AlreadyJoinedMemberException;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
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

    public void checkMemberJoinedCommunity(Long userId, Long communityId) {
        memberRepository.findByUserIdAndCommunityId(userId, communityId)
                .orElseThrow(NotJoinedMemberException::new);
    }

    public boolean hasAuth(Long userId, Long communityId, MemberType memberType) {
        Member member = memberRepository.findByUserIdAndCommunityId(userId, communityId)
                .orElseThrow(() -> new EntityNotFoundException("가입하지 않은 멤버입니다."));

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

    public boolean hasAuthWithoutThrow(Long userId, Long communityId, MemberType memberType) {
        Member member = memberRepository.findByUserIdAndCommunityId(userId, communityId)
                .orElseThrow(() -> new EntityNotFoundException("가입하지 않은 멤버입니다."));

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

        return hasAuth;
    }
}
