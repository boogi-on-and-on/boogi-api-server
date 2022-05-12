package boogi.apiserver.domain.post.postmedia.dao;

import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.List;

import static boogi.apiserver.domain.post.postmedia.domain.QPostMedia.*;

public class PostMediaRepositoryCustomImpl implements PostMediaRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public PostMediaRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<PostMedia> findByIds(List<String> postMediaIds) {
        return queryFactory.selectFrom(postMedia)
                .where(
                        postMedia.uuid.in(postMediaIds),
                        postMedia.post.isNull()
                ).fetch();
    }
}
