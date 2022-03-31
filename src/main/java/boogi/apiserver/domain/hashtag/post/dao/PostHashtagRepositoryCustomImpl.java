package boogi.apiserver.domain.hashtag.post.dao;

import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.List;

import static boogi.apiserver.domain.hashtag.post.domain.QPostHashtag.*;

public class PostHashtagRepositoryCustomImpl implements PostHashtagRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public PostHashtagRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<PostHashtag> findPostHashtagByPostId(Long postId) {
        return queryFactory.selectFrom(postHashtag)
                .where(postHashtag.post.id.eq(postId))
                .fetch();
    }

    @Override
    public void deleteAllByPostId(Long postId) {
        queryFactory.delete(postHashtag)
                .where(postHashtag.post.id.eq(postId))
                .execute();
    }
}
