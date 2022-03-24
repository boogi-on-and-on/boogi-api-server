package boogi.apiserver.domain.community.community.dao;

import boogi.apiserver.domain.community.community.domain.QCommunity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;

public class CommunityRepositoryCustomImpl implements CommunityRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public CommunityRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }



}
