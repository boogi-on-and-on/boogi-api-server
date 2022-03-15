package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.user.dto.UserJoinedCommunity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberRepository memberRepository;

    public List<UserJoinedCommunity> getJoinedMemberInfo(Long userId) {
        return memberRepository.findByUserId(userId).stream()
                .map(m -> UserJoinedCommunity.of(m.getCommunity()))
                .collect(Collectors.toList());
    }
}
