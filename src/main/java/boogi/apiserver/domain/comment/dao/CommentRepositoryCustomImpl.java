package boogi.apiserver.domain.comment.dao;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.domain.QComment;
import boogi.apiserver.domain.member.dao.MemberRepository;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import javax.persistence.EntityManager;
import java.util.List;

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
        List<Long> memberIds = memberRepository.findMemberIdsByUserId(userId);

        List<Comment> comments =
                queryFactory
                        .selectFrom(comment)
                        .where(comment.member.id.in(memberIds))
                        .orderBy(comment.createdAt.desc())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        JPAQuery<Comment> countQuery =
                queryFactory.selectFrom(comment).where(comment.member.id.in(memberIds));

        return PageableExecutionUtils.getPage(comments, pageable, countQuery::fetchCount);
    }
}
