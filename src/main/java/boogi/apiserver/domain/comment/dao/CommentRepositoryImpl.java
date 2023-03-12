package boogi.apiserver.domain.comment.dao;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.global.util.PageableUtil;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

import static boogi.apiserver.domain.comment.domain.QComment.comment;
import static boogi.apiserver.domain.member.domain.QMember.member;


@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Comment> findParentCommentsWithMemberByPostId(Pageable pageable, Long postId) {
        List<Comment> comments = queryFactory.selectFrom(comment)
                .join(comment.member, member).fetchJoin()
                .where(
                        comment.post.id.eq(postId),
                        comment.child.isFalse()
                )
                .orderBy(comment.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return PageableUtil.getSlice(comments, pageable);
    }

    @Override
    public List<Comment> findChildCommentsWithMemberByParentCommentIds(List<Long> commentIds) {
        return queryFactory.selectFrom(comment)
                .join(comment.member, member).fetchJoin()
                .where(comment.parent.id.in(commentIds))
                .orderBy(comment.parent.id.asc(), comment.createdAt.asc())
                .fetch();
    }

    @Override
    public Slice<Comment> getUserCommentPageByMemberIds(List<Long> memberIds, Pageable pageable) {
        List<Comment> findComments = queryFactory.selectFrom(comment)
                .where(comment.member.id.in(memberIds))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .orderBy(comment.createdAt.desc())
                .fetch();

        return PageableUtil.getSlice(findComments, pageable);
    }
}
