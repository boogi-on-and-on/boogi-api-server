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

    public List<PostMedia> findUnmappedPostMediasByUUIDs(List<String> postMediaUUIds) {
        return queryFactory.selectFrom(postMedia)
                .where(
                        postMedia.uuid.in(postMediaUUIds),
                        postMedia.post.isNull(),
                        postMedia.deletedAt.isNull()
                ).fetch();
    }

    @Override
    public List<PostMedia> findByPostId(Long postId) {
        return queryFactory.selectFrom(postMedia)
                .where(
                        postMedia.post.id.eq(postId),
                        postMedia.deletedAt.isNull(),
                        postMedia.canceledAt.isNull()
                ).fetch();
    }
}
