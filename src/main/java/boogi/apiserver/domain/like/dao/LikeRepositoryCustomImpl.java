package boogi.apiserver.domain.like.dao;

import boogi.apiserver.domain.like.domain.Like;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static boogi.apiserver.domain.like.domain.QLike.*;
import static boogi.apiserver.domain.member.domain.QMember.*;
import static com.querydsl.core.group.GroupBy.*;

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

    public Optional<Like> findLikeWithMemberById(Long likeId) {
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
    public List<Like> findCommentLikesByCommentIdsAndMemberId(List<Long> commentIds, Long memberId) {
        return queryFactory.selectFrom(like)
                .where(
                        like.member.id.eq(memberId),
                        like.comment.id.in(commentIds)
                ).fetch();
    }

    public Page<Like> findPostLikePageWithMemberByPostId(Long postId, Pageable pageable) {
        List<Like> result = queryFactory.selectFrom(like)
                .where(
                        like.post.id.eq(postId)
                )
                .join(like.member, member).fetchJoin()
                .orderBy(
                        like.createdAt.asc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Like> countQuery = queryFactory.selectFrom(like)
                .where(
                        like.post.id.eq(postId)
                );

        return PageableExecutionUtils.getPage(result, pageable, () -> countQuery.fetch().size());
    }

    public Page<Like> findCommentLikePageWithMemberByCommentId(Long commentId, Pageable pageable) {
        List<Like> result = queryFactory.selectFrom(like)
                .where(
                        like.comment.id.eq(commentId)
                )
                .join(like.member, member).fetchJoin()
                .orderBy(
                        like.createdAt.asc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Like> countQuery = queryFactory.selectFrom(like)
                .where(
                        like.post.id.eq(commentId)
                );

        return PageableExecutionUtils.getPage(result, pageable, () -> countQuery.fetch().size());
    }

    @Override
    public Map<Long, Long> getCommentLikeCountsByCommentIds(List<Long> commentIds) {
        return queryFactory.select(like.count())
                .from(like)
                .where(
                        like.comment.id.in(commentIds)
                )
                .groupBy(like.comment.id)
                .transform(groupBy(like.comment.id).as(like.count()));
    }
}