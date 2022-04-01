package boogi.apiserver.domain.like.dao;

import boogi.apiserver.domain.like.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;


public interface LikeRepository extends JpaRepository<Like, Long>, LikeRepositoryCustom {
}
