package boogi.apiserver.domain.comment.dao;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.domain.QComment;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.support.PageableExecutionUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static boogi.apiserver.domain.member.domain.QMember.*;


public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Autowired
    private MemberRepository memberRepository;

    private final QComment comment = QComment.comment;

    public CommentRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Comment> getUserCommentPage(Pageable pageable, Long userId) {
        List<Long> memberIds = memberRepository.findByUserId(userId)
                .stream()
                .map(Member::getId)
                .collect(Collectors.toList());


        List<Comment> comments =
                queryFactory
                        .selectFrom(comment)
                        .where(
                                comment.member.id.in(memberIds)
                        )
                        .orderBy(comment.createdAt.desc())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        JPAQuery<Comment> countQuery =
                queryFactory.selectFrom(comment).where(comment.member.id.in(memberIds));

        return PageableExecutionUtils.getPage(comments, pageable, countQuery::fetchCount);
    }

    @Override
    public Optional<Comment> findCommentWithMemberByCommentId(Long commentId) {
        Comment result = queryFactory.selectFrom(this.comment)
                .join(this.comment.member, member).fetchJoin()
                .where(
                        this.comment.id.eq(commentId)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Slice<Comment> findParentCommentsWithMemberByPostId(Pageable pageable, Long postId) {
        int pageSize = pageable.getPageSize();

        List<Comment> comments = queryFactory.selectFrom(comment)
                .join(comment.member, member).fetchJoin()
                .where(
                        comment.post.id.eq(postId),
                        comment.child.isFalse()
                )
                .orderBy(
                        comment.createdAt.asc()
                )
                .offset(pageable.getOffset())
                .limit(pageSize + 1)
                .fetch();

        boolean hasNext = false;
        if (comments.size() > pageSize) {
            comments.remove(pageSize);
            hasNext = true;
        }
        return new SliceImpl<>(comments, pageable, hasNext);
    }

    @Override
    public List<Comment> findChildCommentsWithMemberByParentCommentIds(List<Long> commentIds) {
        return queryFactory.selectFrom(comment)
                .join(comment.member, member).fetchJoin()
                .where(
                        comment.parent.id.in(commentIds)
                )
                .orderBy(
                        comment.parent.id.asc(),
                        comment.createdAt.asc()
                ).fetch();
    }

    @Override
    public Optional<Comment> findCommentById(Long commentId) {
        Comment findComment = queryFactory.selectFrom(this.comment)
                .where(
                        this.comment.id.eq(commentId)
                ).fetchOne();

        return Optional.ofNullable(findComment);
    }

    @Override
    public Slice<Comment> getUserCommentPageByMemberIds(List<Long> memberIds, Pageable pageable) {
        int pageSize = pageable.getPageSize();

        List<Comment> findComments = queryFactory.selectFrom(comment)
                .where(
                        comment.member.id.in(memberIds)
                )
                .offset(pageable.getOffset())
                .limit(pageSize + 1)
                .orderBy(comment.createdAt.desc())
                .fetch();

        boolean hasNext = false;
        if (findComments.size() > pageSize) {
            findComments.remove(pageSize);
            hasNext = true;
        }

        return new SliceImpl<>(findComments, pageable, hasNext);
    }
}
