package boogi.apiserver.domain.member.dao;

import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.dto.dto.BannedMemberDto;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;

public interface MemberRepositoryCustom {

    List<Member> findByUserId(Long userId);

    List<Member> findMembersWithCommunity(Long userId);

    Optional<Member> findByUserIdAndCommunityId(Long userId, Long communityId);

    Slice<Member> findJoinedMembers(Pageable pageable, Long communityId);

    List<Member> findAllJoinedMembersWithUser(Long communityId);

    Optional<Member> findAnyMemberExceptManager(Long communityId);

    List<BannedMemberDto> findBannedMembers(Long communityId);

    List<Member> findAlreadyJoinedMember(List<Long> userIds, Long communityId);

    List<Long> findMemberIdsForQueryUserPost(Long userId, Long sessionUserId);

    List<Long> findMemberIdsForQueryUserPost(Long sessionUserId);

    Member findManager(Long communityId);

    Slice<UserBasicProfileDto> findMentionMember(Pageable pageable, Long communityId, String name);
}
