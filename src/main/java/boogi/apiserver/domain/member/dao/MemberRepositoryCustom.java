package boogi.apiserver.domain.member.dao;

import boogi.apiserver.domain.member.domain.Member;

import java.util.List;

public interface MemberRepositoryCustom {

    List<Member> findByUserId(Long userId);
    List<Member> findWhatIJoined(Long userId);

    List<Member> findByUserIdAndCommunityId(Long userId, Long communityId);
}
