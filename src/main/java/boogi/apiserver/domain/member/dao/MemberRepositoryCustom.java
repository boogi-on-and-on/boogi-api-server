package boogi.apiserver.domain.member.dao;

import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.dto.response.BannedMemberDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MemberRepositoryCustom {

    List<Member> findByUserId(Long userId);

    List<Member> findWhatIJoined(Long userId);

    Optional<Member> findByUserIdAndCommunityId(Long userId, Long communityId);

    Page<Member> findJoinedMembers(Pageable pageable, Long communityId);

    List<Member> findJoinedMembersAllWithUserByCommunityId(Long communityId);

    Optional<Member> findAnyMemberExceptManager(Long communityId);

    List<BannedMemberDto> findBannedMembers(Long communityId);

    List<Member> findAlreadyJoinedMemberByUserId(List<Long> userIds, Long communityId);

    List<Long> findMemberIdsForQueryUserPostByUserIdAndSessionUserId(Long userId, Long sessionUserId);

    List<Long> findMemberIdsForQueryUserPostBySessionUserId(Long sessionUserId);

    Member findManager(Long communityId);
}
