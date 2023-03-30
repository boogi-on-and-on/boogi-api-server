package boogi.apiserver.domain.like.dao;

import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.global.util.PageableUtil;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.*;

import static boogi.apiserver.domain.like.domain.QLike.like;
import static boogi.apiserver.domain.member.domain.QMember.member;
import static com.querydsl.core.group.GroupBy.groupBy;


@RequiredArgsConstructor
public class LikeRepositoryImpl implements LikeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

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
        List<Long> result = queryFactory.select(like.id)
                .from(like)
                .where(
                        like.post.id.eq(postId),
                        like.member.id.eq(memberId)
                ).fetch();

        return !result.isEmpty();
    }

    @Override
    public boolean existsLikeByCommentIdAndMemberId(Long commentId, Long memberId) {
        List<Long> result = queryFactory.select(like.id)
                .from(like)
                .where(
                        like.comment.id.eq(commentId),
                        like.member.id.eq(memberId)
                ).fetch();

        return !result.isEmpty();
    }

    @Override
    public List<Like> findCommentLikesByCommentIdsAndMemberId(List<Long> commentIds, Long memberId) {
        if (memberId == null) {
            return new ArrayList<>();
        }
        return queryFactory.selectFrom(like)
                .where(
                        like.member.id.eq(memberId),
                        like.comment.id.in(commentIds)
                ).fetch();
    }

    public Slice<Like> findPostLikePageWithMemberByPostId(Long postId, Pageable pageable) {
        List<Like> postLikes = queryFactory.selectFrom(like)
                .where(
                        like.post.id.eq(postId)
                )
                .join(like.member, member).fetchJoin()
                .orderBy(like.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return PageableUtil.getSlice(postLikes, pageable);
    }

    public Slice<Like> findCommentLikePageWithMemberByCommentId(Long commentId, Pageable pageable) {
        List<Like> commentLikes = queryFactory.selectFrom(like)
                .where(
                        like.comment.id.eq(commentId)
                )
                .join(like.member, member).fetchJoin()
                .orderBy(like.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return PageableUtil.getSlice(commentLikes, pageable);
    }

    @Override
    public Map<Long, Long> getCommentLikeCountsByCommentIds(List<Long> commentIds) {
        if (commentIds.size() == 0) {
            return new HashMap<>();
        }
        return queryFactory.select(like.count())
                .from(like)
                .where(
                        like.comment.id.in(commentIds)
                )
                .groupBy(like.comment.id)
                .transform(groupBy(like.comment.id).as(like.count()));
    }
}