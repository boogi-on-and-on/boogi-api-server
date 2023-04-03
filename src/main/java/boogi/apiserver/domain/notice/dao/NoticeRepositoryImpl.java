package boogi.apiserver.domain.notice.dao;

import boogi.apiserver.domain.notice.domain.Notice;
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
    public List<Notice> getLatestAppNotice() {
        return getAppNotice(3);
    }

    @Override
    public List<Notice> getLatestNotice(Long communityId) {
        return getNotice(communityId, 3);
    }

    @Override
    public List<Notice> getAllAppNotices() {
        return getAppNotice(null);
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

    private List<Notice> getNotice(Long communityId, Integer limit) {
        return queryFactory.selectFrom(notice)
                .where(
                        notice.community.id.eq(communityId)
                )
                .orderBy(notice.createdAt.desc())
                .limit(resolveInvalidLimit(limit))
                .fetch();
    }

    private List<Notice> getAppNotice(Integer limit) {
        return queryFactory.selectFrom(notice)
                .where(
                        notice.community.isNull()
                )
                .orderBy(notice.createdAt.desc())
                .limit(resolveInvalidLimit(limit))
                .fetch();
    }

    private Integer resolveInvalidLimit(Integer limit) {
        if (limit == null) {
            return Integer.MAX_VALUE;
        } else if (limit < 0) {
            throw new IllegalArgumentException("limit는 0 보다 작을 수 없습니다.");
        }
        return limit;
    }
}
