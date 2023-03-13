package boogi.apiserver.domain.community.joinrequest.dao;

import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.community.joinrequest.exception.JoinRequestNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long>, JoinRequestRepositoryCustom {

    default JoinRequest findByJoinRequestId(Long joinRequestId) {
        return this.findById(joinRequestId).orElseThrow(JoinRequestNotFoundException::new);
    }
}
