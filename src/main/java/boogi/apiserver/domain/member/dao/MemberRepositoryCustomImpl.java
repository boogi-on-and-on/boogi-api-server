package boogi.apiserver.domain.member.dao;

import boogi.apiserver.domain.member.domain.QMember;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    private final QMember member = QMember.member;

    public MemberRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Long> findMemberIdsByUserId(Long userId) {
        return queryFactory.select(member.id)
                .from(member)
                .where(member.user.id.eq(userId))
                .fetch();
    }
}
