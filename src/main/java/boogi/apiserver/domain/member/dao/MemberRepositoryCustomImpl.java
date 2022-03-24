package boogi.apiserver.domain.member.dao;

import boogi.apiserver.domain.community.community.domain.QCommunity;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.domain.QMember;
import boogi.apiserver.domain.user.domain.QUser;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import javax.persistence.EntityManager;
import java.util.List;

public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    private final QMember member = QMember.member;
    private final QCommunity community = QCommunity.community;
    private final QUser user = QUser.user;

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

    @Override
    public Page<Member> findJoinedMembers(Pageable pageable, Long communityId) {
        NumberExpression<Integer> caseBuilder = new CaseBuilder()
                .when(member.memberType.eq(MemberType.MANAGER)).then(3)
                .when(member.memberType.eq(MemberType.SUB_MANAGER)).then(2)
                .when(member.memberType.eq(MemberType.NORMAL)).then(1)
                .otherwise(0);

        List<Member> members = queryFactory.select(member)
                .from(member)
                .where(
                        member.community.id.eq(communityId),
                        member.canceledAt.isNull(),
                        member.user.canceledAt.isNull()
                )
                .innerJoin(member.user, user).fetchJoin()
                .orderBy(
                        caseBuilder.desc(),
                        member.createdAt.asc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Member> countQuery = queryFactory
                .selectFrom(member)
                .where(
                        member.community.id.eq(communityId),
                        member.canceledAt.isNull(),
                        member.user.canceledAt.isNull()
                )
                .join(member.user, user);

        return PageableExecutionUtils.getPage(members, pageable, countQuery::fetchCount);
    }

}
