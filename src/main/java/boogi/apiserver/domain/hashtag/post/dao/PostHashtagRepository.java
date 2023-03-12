package boogi.apiserver.domain.hashtag.post.dao;

import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PostHashtagRepository extends JpaRepository<PostHashtag, Long> {
}
