package boogi.apiserver.domain.community.community.dao;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.exception.CommunityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long>, CommunityRepositoryCustom {

    Optional<Community> findByCommunityNameEquals(String name);

    default Community findCommunityById(Long communityId) {
        return this.findById(communityId).orElseThrow(CommunityNotFoundException::new);
    }
}
