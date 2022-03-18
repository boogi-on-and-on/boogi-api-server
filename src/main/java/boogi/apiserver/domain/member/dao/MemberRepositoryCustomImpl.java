package boogi.apiserver.domain.member.dao;

import boogi.apiserver.domain.community.community.domain.QCommunity;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.QMember;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    private final QMember member = QMember.member;
    private final QCommunity community = QCommunity.community;

    public MemberRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Member> findByUserId(Long userId) {
        return queryFactory.select(member)
                .from(member)
                .where(member.user.id.eq(userId), member.canceledAt.isNull())
                .fetch();
    }

    @Override
    public List<Member> findWhatIJoined(Long userId) {
        return queryFactory.selectFrom(member)
                .where(member.user.id.eq(userId),
                        member.bannedAt.isNull(),
                        member.canceledAt.isNull(),
                        member.community.deletedAt.isNull()
                )
                .join(member.community).fetchJoin()
                .fetch();
    }

    @Override
    public List<Member> findByUserIdAndCommunityId(Long userId, Long communityId) {
        return queryFactory.selectFrom(member)
                .where(
                        member.user.id.eq(userId),
                        member.community.id.eq(communityId),
                        member.canceledAt.isNull(),
                        member.bannedAt.isNull()
                )
                .fetch();
    }
}
