package boogi.apiserver.domain.message.block.dao;

import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.domain.message.block.domain.QMessageBlock;
import boogi.apiserver.domain.message.block.dto.MessageBlockedUserDto;
import boogi.apiserver.domain.message.block.dto.QMessageBlockedUserDto;
import boogi.apiserver.domain.user.domain.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class MessageBlockRepositoryCustomImpl implements MessageBlockRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QMessageBlock messageBlock = QMessageBlock.messageBlock;
    private final QUser user = QUser.user;

    public MessageBlockRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MessageBlockedUserDto> getBlockedUsers(Long userId) {
        return queryFactory.select(new QMessageBlockedUserDto(
                        messageBlock.blockedUser.id,
                        messageBlock.blockedUser.username,
                        messageBlock.blockedUser.tagNumber
                ))
                .from(messageBlock)
                .where(
                        messageBlock.user.id.eq(userId),
                        messageBlock.blocked.eq(true),
                        messageBlock.canceledAt.isNull(),
                        messageBlock.user.canceledAt.isNull()
                ).innerJoin(messageBlock.user, user)
                .fetch();
    }

    @Override
    public MessageBlock getMessageBlockByUserId(Long userId, Long blockedUserId) {
        return queryFactory.selectFrom(messageBlock)
                .where(
                        messageBlock.user.id.eq(userId),
                        messageBlock.blockedUser.id.eq(blockedUserId),
                        messageBlock.canceledAt.isNull()
                )
                .fetchOne();
    }
}
