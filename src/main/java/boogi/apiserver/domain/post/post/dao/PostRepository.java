package boogi.apiserver.domain.post.post.dao;

import boogi.apiserver.domain.post.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    @Query(value = "SELECT post_id, community_id, member_id, content, deleted_at, like_count, comment_count, canceled_at, created_at, updated_at " +
            "FROM (SELECT *, RANK() OVER (PARTITION BY p.community_id ORDER BY p.created_at DESC) AS a " +
            "FROM post AS p " +
            "WHERE p.community_id IN (:communityIds) " +
            "AND p.deleted_at is NULL and p.canceled_at is NULL) AS rankrow " +
            "WHERE rankrow.a <= 1 " +
            "ORDER BY created_at DESC",
            nativeQuery = true)
    List<Post> getLatestPostByCommunityIds(@Param("communityIds") Set<Long> communityIds);
}

