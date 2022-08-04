package boogi.apiserver.domain.notice.dao;

import boogi.apiserver.domain.member.domain.QMember;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.notice.domain.QNotice;
import boogi.apiserver.domain.user.domain.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class NoticeRepositoryCustomImpl implements NoticeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QNotice notice = QNotice.notice;
    private final QMember member = QMember.member;
    private final QUser user = QUser.user;

    public NoticeRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Notice> getLatestNotice(Long communityId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(notice.community.id.eq(communityId));

        return queryNotice(builder, 3);
    }

    @Override
    public List<Notice> getLatestNotice() {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(notice.community.isNull());

        return queryNotice(builder, 3);
    }

    @Override
    public List<Notice> getAllNotices(Long communityId) {
        return queryFactory.selectFrom(notice)
                .where(
                        notice.community.id.eq(communityId)
                )
                .join(notice.member, member).fetchJoin()
                .join(member.user, user).fetchJoin()
                .orderBy(notice.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Notice> getAllNotices() {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(notice.community.isNull());

        return queryNotice(builder, null);
    }

    private List<Notice> queryNotice(BooleanBuilder builder, Integer limit) {
        return queryFactory.selectFrom(notice)
                .where(builder)
                .orderBy(notice.createdAt.desc())
                .limit(limit != null ? 3 : Integer.MAX_VALUE)
                .fetch();
    }
}
