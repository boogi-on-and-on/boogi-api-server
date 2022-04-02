package boogi.apiserver.domain.like.dao;

import boogi.apiserver.domain.like.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long>, LikeRepositoryCustom {
}
