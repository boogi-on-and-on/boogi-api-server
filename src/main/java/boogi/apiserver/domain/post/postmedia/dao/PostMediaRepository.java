package boogi.apiserver.domain.post.postmedia.dao;

import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostMediaRepository extends JpaRepository<PostMedia, Long>, PostMediaRepositoryCustom {

    @Query(value = "SELECT post_media_id, post_id, media_type, media_url, uuid, deleted_at, created_at, updated_at, canceled_at " +
            "FROM (SELECT *, RANK() OVER (PARTITION BY pm.post_id ORDER BY pm.created_at) AS a " +
            "FROM post_media AS pm " +
            "WHERE pm.post_id IN (:latestPostIds)) AS rankrow " +
            "WHERE rankrow.a <= 1",
            nativeQuery = true)
    List<PostMedia> getPostMediasByLatestPostIds(@Param("latestPostIds") List<Long> latestedPostIds);

    List<PostMedia> findByUuidIn(List<String> uuid);
}
