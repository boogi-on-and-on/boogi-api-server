package boogi.apiserver.domain.community.community.dao;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.domain.QCommunity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.Optional;

public class CommunityRepositoryCustomImpl implements CommunityRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public CommunityRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    @Override
    public Optional<Community> findCommunityById(Long communityId) {
        Community findCommunity = queryFactory.selectFrom(QCommunity.community)
                .where(
                        QCommunity.community.id.eq(communityId),
                        QCommunity.community.deletedAt.isNull(),
                        QCommunity.community.canceledAt.isNull()
                ).fetchOne();

        return Optional.ofNullable(findCommunity);
    }
}
