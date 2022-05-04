package boogi.apiserver.domain.message.message.dao;


import boogi.apiserver.domain.message.message.domain.Message;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import javax.persistence.EntityManager;
import java.util.List;

import static boogi.apiserver.domain.message.message.domain.QMessage.*;

public class MessageRepositoryCustomImpl implements MessageRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MessageRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Message> findMessagesByOpponentIdAndMyId(Long opponentId, Long myId, Pageable pageable) {
        List<Message> messages = queryFactory.selectFrom(message)
                .where(
                        message.sender.id.eq(opponentId).and(message.receiver.id.eq(myId))
                                .or(message.receiver.id.eq(opponentId).and(message.sender.id.eq(myId))),
                        message.blocked_message.isFalse(),
                        message.canceledAt.isNull()
                )
                .orderBy(message.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Message> countQuery = queryFactory.selectFrom(message)
                .where(
                        message.sender.id.eq(opponentId).and(message.receiver.id.eq(myId))
                                .or(message.receiver.id.eq(opponentId).and(message.sender.id.eq(myId))),
                        message.blocked_message.isFalse(),
                        message.canceledAt.isNull()
                );

        return PageableExecutionUtils.getPage(messages, pageable, () -> countQuery.fetch().size());
    }
}