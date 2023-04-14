package boogi.apiserver.domain.community.joinrequest.repository;

import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;

import java.util.List;
import java.util.Optional;

public interface JoinRequestRepositoryCustom {

    List<JoinRequest> getAllPendingRequests(Long communityId);

    Optional<JoinRequest> getLatestJoinRequest(Long userId, Long communityId);

    List<JoinRequest> getRequestsByIds(List<Long> requestIds);
}
