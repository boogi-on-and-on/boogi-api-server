package boogi.apiserver.domain.community.joinrequest.dao;

import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long>, JoinRequestRepositoryCustom {

}
