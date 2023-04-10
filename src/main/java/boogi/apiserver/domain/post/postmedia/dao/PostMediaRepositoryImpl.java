package boogi.apiserver.domain.post.postmedia.dao;

import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static boogi.apiserver.domain.post.postmedia.domain.QPostMedia.postMedia;

@RequiredArgsConstructor
public class PostMediaRepositoryImpl implements PostMediaRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PostMedia> findUnmappedPostMedias(List<String> postMediaUUIDs) {
        return queryFactory.selectFrom(postMedia)
                .where(
                        postMedia.uuid.in(postMediaUUIDs),
                        postMedia.post.isNull()
                ).fetch();
    }
}
