package boogi.apiserver.domain.like.dao;

import boogi.apiserver.domain.like.domain.Like;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static boogi.apiserver.domain.like.domain.QLike.*;
import static boogi.apiserver.domain.member.domain.QMember.*;

public class LikeRepositoryCustomImpl implements LikeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public LikeRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Like> findPostLikesByPostId(Long postId) {
        return queryFactory.selectFrom(like)
                .where(like.post.id.eq(postId))
                .fetch();
    }

    @Override
    public void deleteAllPostLikeByPostId(Long postId) {
        queryFactory.delete(like)
                .where(
                        like.post.id.eq(postId)
                ).execute();
    }

    @Override
    public void deleteAllCommentLikeByCommentId(Long commentId) {
        queryFactory.delete(like)
                .where(
                        like.comment.id.eq(commentId)
                ).execute();
    }

    @Override
    public boolean existsLikeByPostIdAndMemberId(Long postId, Long memberId) {
        Long result = queryFactory.select(like.id)
                .from(like)
                .where(
                        like.post.id.eq(postId),
                        like.member.id.eq(memberId)
                ).fetchFirst();

        return (result == null) ? false : true;
    }

    @Override
    public boolean existsLikeByCommentIdAndMemberId(Long commentId, Long memberId) {
        Long result = queryFactory.select(like.id)
                .from(like)
                .where(
                        like.comment.id.eq(commentId),
                        like.member.id.eq(memberId)
                ).fetchFirst();

        return (result == null) ? false : true;
    }

    @Override
    public Optional<Like> findPostLikeWithMemberByLikeId(Long likeId) {
        Like result = queryFactory.selectFrom(like)
                .join(like.member, member).fetchJoin()
                .where(like.id.eq(likeId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Like> findPostLikeByPostIdAndMemberId(Long postId, Long memberId) {
        Like findLike = queryFactory.selectFrom(like)
                .where(
                        like.post.id.eq(postId),
                        like.member.id.eq(memberId)
                ).fetchOne();

        return Optional.ofNullable(findLike);
    }

    @Override
    public Optional<Like> findCommentLikeByCommentIdAndMemberId(Long commentId, Long memberId) {
        throw new RuntimeException("구현 안함");
    }
}
