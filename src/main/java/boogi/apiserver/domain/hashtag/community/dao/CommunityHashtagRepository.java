package boogi.apiserver.domain.hashtag.community.dao;

import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityHashtagRepository extends JpaRepository<CommunityHashtag, Long> {

}
