package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.AlreadyJoinedMemberException;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Member checkMemberJoinedCommunity(Long userId, Long communityId) {
        List<Member> members = memberRepository.findByUserIdAndCommunityId(userId, communityId);
        if (members.isEmpty()) {
            throw new NotJoinedMemberException();
        }
        return members.get(0);
    }
}
