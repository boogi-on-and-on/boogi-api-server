package boogi.apiserver.domain.message.message.dao;


import boogi.apiserver.domain.message.message.domain.Message;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.List;

import static boogi.apiserver.domain.message.message.domain.QMessage.*;

public class MessageRepositoryCustomImpl implements MessageRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MessageRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

}