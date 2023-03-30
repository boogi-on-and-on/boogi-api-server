package boogi.apiserver.domain.like.dao;

import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.exception.LikeNotFoundException;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long>, LikeRepositoryCustom {

    Optional<Like> findPostLikeByPostAndMember(Post post, Member member);

    default Like findLikeById(Long likeId) {
        return this.findById(likeId).orElseThrow(LikeNotFoundException::new);
    }
}
