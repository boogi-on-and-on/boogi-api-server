package boogi.apiserver.domain.community.joinrequest.dao;

import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;

import java.util.List;

public interface JoinRequestRepositoryCustom {

    List<JoinRequest> getAllRequests(Long communityId);

    JoinRequest getLatestJoinRequest(Long userId, Long communityId);
}
