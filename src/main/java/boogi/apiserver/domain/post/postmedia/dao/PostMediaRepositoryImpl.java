package boogi.apiserver.domain.post.postmedia.dao;

import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.vo.PostMedias;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static boogi.apiserver.domain.post.postmedia.domain.QPostMedia.postMedia;

@RequiredArgsConstructor
public class PostMediaRepositoryImpl implements PostMediaRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public PostMedias findUnmappedPostMediasByUUIDs(List<String> postMediaUUIds) {
        return new PostMedias(
                queryFactory.selectFrom(postMedia)
                        .where(
                                postMedia.uuid.in(postMediaUUIds),
                                postMedia.post.isNull(),
                                postMedia.deletedAt.isNull()
                        ).fetch()
        );
    }

    @Override
    public List<PostMedia> findUnmappedPostMedias2(List<String> postMediaUUIds) {
        return queryFactory.selectFrom(postMedia)
                .where(postMedia.uuid.in(postMediaUUIds),
                        postMedia.post.isNull(),
                        postMedia.deletedAt.isNull()
                ).fetch();
    }

    @Override
    public List<PostMedia> findByPostId(Long postId) {
        return queryFactory.selectFrom(postMedia)
                .where(
                        postMedia.post.id.eq(postId),
                        postMedia.deletedAt.isNull()
                ).fetch();
    }
}
