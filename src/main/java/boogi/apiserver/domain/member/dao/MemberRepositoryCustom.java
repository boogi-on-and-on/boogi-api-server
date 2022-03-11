package boogi.apiserver.domain.member.dao;

import java.util.List;

public interface MemberRepositoryCustom {

    List<Long> findMemberIdsByUserId(Long userId);
}
