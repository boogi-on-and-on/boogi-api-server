package boogi.apiserver.domain.community.community.dao;

import boogi.apiserver.domain.community.community.domain.Community;

import java.util.Optional;

public interface CommunityRepositoryCustom {

    Optional<Community> findCommunityById(Long communityId);
}
