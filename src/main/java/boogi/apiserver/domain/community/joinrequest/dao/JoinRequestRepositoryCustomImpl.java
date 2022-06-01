package boogi.apiserver.domain.community.joinrequest.dao;

import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequestStatus;
import boogi.apiserver.domain.community.joinrequest.domain.QJoinRequest;
import boogi.apiserver.domain.user.domain.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class JoinRequestRepositoryCustomImpl implements JoinRequestRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    private final QJoinRequest joinRequest = QJoinRequest.joinRequest;
    private final QUser user = QUser.user;

    public JoinRequestRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<JoinRequest> getAllRequests(Long communityId) {
        return queryFactory.selectFrom(joinRequest)
                .where(
                        joinRequest.community.id.eq(communityId),
                        joinRequest.status.eq(JoinRequestStatus.PENDING),
                        joinRequest.confirmedMember.isNull(),
                        joinRequest.acceptor.isNull(),
                        joinRequest.canceledAt.isNull(),
                        joinRequest.user.canceledAt.isNull()
                )
                .orderBy(joinRequest.createdAt.desc())
                .join(joinRequest.user, user).fetchJoin()
                .fetch();
    }

    @Override
    public JoinRequest getLatestJoinRequest(Long userId, Long communityId) {
        return queryFactory.selectFrom(joinRequest)
                .where(
                        joinRequest.user.id.eq(userId),
                        joinRequest.community.id.eq(communityId),
                        joinRequest.canceledAt.isNull()
                )
                .orderBy(joinRequest.createdAt.desc())
                .limit(1)
                .fetchOne();
    }

    @Override
    public List<JoinRequest> getRequestsByIds(List<Long> requestIds) {
        return queryFactory.selectFrom(joinRequest)
                .where(joinRequest.id.in(requestIds), joinRequest.canceledAt.isNull())
                .fetch();
    }

}
