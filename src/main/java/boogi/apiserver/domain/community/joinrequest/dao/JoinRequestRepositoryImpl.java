package boogi.apiserver.domain.community.joinrequest.dao;

import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequestStatus;
import boogi.apiserver.domain.community.joinrequest.domain.QJoinRequest;
import boogi.apiserver.domain.user.domain.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
public class JoinRequestRepositoryImpl implements JoinRequestRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    private final QJoinRequest joinRequest = QJoinRequest.joinRequest;
    private final QUser user = QUser.user;

    @Override
    public List<JoinRequest> getAllRequests(Long communityId) {
        return queryFactory.selectFrom(joinRequest)
                .where(
                        joinRequest.community.id.eq(communityId),
                        joinRequest.status.eq(JoinRequestStatus.PENDING),
                        joinRequest.confirmedMember.isNull(),
                        joinRequest.acceptor.isNull()
                )
                .orderBy(joinRequest.createdAt.desc())
                .join(joinRequest.user, user).fetchJoin()
                .fetch();
    }

    @Override
    public Optional<JoinRequest> getLatestJoinRequest(Long userId, Long communityId) {
        JoinRequest request = queryFactory.selectFrom(joinRequest)
                .where(
                        joinRequest.user.id.eq(userId),
                        joinRequest.community.id.eq(communityId)
                )
                .orderBy(joinRequest.createdAt.desc())
                .limit(1)
                .fetchOne();

        return Optional.ofNullable(request);
    }

    @Override
    public List<JoinRequest> getRequestsByIds(List<Long> requestIds) {
        return queryFactory.selectFrom(joinRequest)
                .where(joinRequest.id.in(requestIds))
                .fetch();
    }

}
