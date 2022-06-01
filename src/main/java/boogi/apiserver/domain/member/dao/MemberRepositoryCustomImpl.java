package boogi.apiserver.domain.member.dao;

import boogi.apiserver.domain.community.community.domain.QCommunity;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.domain.QMember;
import boogi.apiserver.domain.member.dto.BannedMemberDto;
import boogi.apiserver.domain.member.dto.QBannedMemberDto;
import boogi.apiserver.domain.user.domain.QUser;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
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
                .where(member.user.id.eq(userId),
                        member.canceledAt.isNull(),
                        member.bannedAt.isNull()
                )
                .fetch();
    }

    @Override
    public List<Member> findWhatIJoined(Long userId) {
        return queryFactory.selectFrom(member)
                .where(member.user.id.eq(userId),
                        member.bannedAt.isNull(),
                        member.canceledAt.isNull(),
                        member.community.deletedAt.isNull(),
                        member.community.canceledAt.isNull()
                )
                .join(member.community).fetchJoin()
                .orderBy(member.createdAt.desc())
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
                        member.user.canceledAt.isNull(),
                        member.bannedAt.isNull()
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
                        member.bannedAt.isNull(),
                        member.user.canceledAt.isNull()
                )
                .join(member.user, user);

        return PageableExecutionUtils.getPage(members, pageable, countQuery::fetchCount);
    }

    @Override
    public List<Member> findJoinedMembersAllWithUserByCommunityId(Long communityId) {
        return queryFactory.selectFrom(member)
                .where(
                        member.community.id.eq(communityId),
                        member.canceledAt.isNull(),
                        member.bannedAt.isNull()
                )
                .join(member.user, user).fetchJoin()
                .fetch();
    }

    @Override
    public Member findAnyMemberExceptManager(Long communityId) {
        return queryFactory
                .selectFrom(this.member)
                .where(
                        this.member.community.id.eq(communityId),
                        this.member.memberType.ne(MemberType.MANAGER),
                        this.member.bannedAt.isNull(),
                        this.member.canceledAt.isNull()
                ).limit(1)
                .fetchOne();
    }

    @Override
    public List<BannedMemberDto> findBannedMembers(Long communityId) {
        return queryFactory
                .select(new QBannedMemberDto(member.id, member.user))
                .from(member)
                .where(
                        member.community.id.eq(communityId),
                        member.bannedAt.isNotNull(),
                        member.canceledAt.isNull()
                )
                .join(member.user, user)
                .orderBy(member.bannedAt.desc())
                .fetch();
    }

    @Override
    public List<Member> findAlreadyJoinedMemberByUserId(List<Long> userIds, Long communityId) {
        return queryFactory
                .selectFrom(member)
                .where(member.community.id.eq(communityId),
                        member.user.id.in(userIds),
                        member.canceledAt.isNull()
                ).fetch();
    }

    @Override
    public List<Long> findMemberIdsForQueryUserPostByUserIdAndSessionUserId(Long userId, Long sessionUserId) {
        QMember memberSub = new QMember("memberSub");

        return queryFactory.select(member.id)
                .from(member)
                .where(
                        member.user.id.eq(userId),
                        member.bannedAt.isNull(),
                        member.canceledAt.isNull(),
                        member.community.id.notIn(
                                JPAExpressions
                                        .select(community.id)
                                        .from(community)
                                        .where(
                                                community.isPrivate.isTrue(),
                                                community.deletedAt.isNull(),
                                                community.canceledAt.isNull(),
                                                community.id.in(
                                                        JPAExpressions
                                                                .select(memberSub.community.id)
                                                                .from(memberSub)
                                                                .where(
                                                                        memberSub.user.id.in(userId, sessionUserId),
                                                                        memberSub.bannedAt.isNull(),
                                                                        memberSub.canceledAt.isNull()
                                                                )
                                                                .groupBy(memberSub.community.id)
                                                                .having(memberSub.community.id.count().lt(2))
                                                )
                                        )
                        )
                ).fetch();
    }

    @Override
    public List<Long> findMemberIdsForQueryUserPostBySessionUserId(Long sessionUserId) {
        return queryFactory.select(member.id)
                .from(member)
                .where(
                        member.user.id.eq(sessionUserId),
                        member.bannedAt.isNull(),
                        member.canceledAt.isNull()
                ).fetch();
    }
}
