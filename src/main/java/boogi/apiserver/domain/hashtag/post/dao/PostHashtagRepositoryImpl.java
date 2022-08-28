package boogi.apiserver.domain.hashtag.post.dao;

import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static boogi.apiserver.domain.hashtag.post.domain.QPostHashtag.postHashtag;


@RequiredArgsConstructor
public class PostHashtagRepositoryImpl implements PostHashtagRepositoryCustom {

    private final JPAQueryFactory queryFactory;

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
