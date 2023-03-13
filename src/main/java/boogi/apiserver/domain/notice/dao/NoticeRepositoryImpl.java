package boogi.apiserver.domain.notice.dao;

import boogi.apiserver.domain.notice.domain.Notice;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static boogi.apiserver.domain.member.domain.QMember.member;
import static boogi.apiserver.domain.notice.domain.QNotice.notice;
import static boogi.apiserver.domain.user.domain.QUser.user;

@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Notice> getLatestNotice(Long communityId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(notice.community.id.eq(communityId));

        return queryNotice(builder, 3);
    }

    @Override
    public List<Notice> getLatestAppNotice() {
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
