package boogi.apiserver.domain.message.block.dao;

import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.domain.message.block.domain.QMessageBlock;
import boogi.apiserver.domain.message.block.dto.dto.MessageBlockedUserDto;
import boogi.apiserver.domain.message.block.dto.dto.QMessageBlockedUserDto;
import boogi.apiserver.domain.user.domain.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class MessageBlockRepositoryImpl implements MessageBlockRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QMessageBlock messageBlock = QMessageBlock.messageBlock;
    private final QUser user = QUser.user;

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
                        messageBlock.blocked.eq(true)
                )
                .fetch();
    }

    @Override
    public MessageBlock getMessageBlockByUserId(Long userId, Long blockedUserId) {
        return queryFactory.selectFrom(messageBlock)
                .where(
                        messageBlock.user.id.eq(userId),
                        messageBlock.blockedUser.id.eq(blockedUserId)
                )
                .fetchOne();
    }

    @Override
    public List<MessageBlock> getMessageBlocksByUserIds(Long userId, List<Long> blockedUserIds) {
        return queryFactory.selectFrom(messageBlock)
                .where(
                        messageBlock.user.id.eq(userId),
                        messageBlock.blockedUser.id.in(blockedUserIds)
                )
                .fetch();
    }

    @Override
    public void updateBulkBlockedStatus(List<Long> blockUserIds) {
        queryFactory.update(messageBlock)
                .set(messageBlock.blocked, true)
                .where(messageBlock.blockedUser.id.in(blockUserIds))
                .execute();
    }

    @Override
    public Boolean checkOnlyReceiverBlockedFromSender(Long senderId, Long receiverId) {
        MessageBlock findMessageBlock = queryFactory.selectFrom(messageBlock)
                .where(
                        messageBlock.user.id.eq(senderId),
                        messageBlock.blockedUser.id.eq(receiverId),
                        messageBlock.blocked.isTrue()
                ).fetchFirst();
        return (findMessageBlock == null) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public List<MessageBlock> findMessageBlocksByUserId(Long userId) {
        return queryFactory.selectFrom(messageBlock)
                .where(
                        messageBlock.user.id.eq(userId),
                        messageBlock.blocked.isTrue()
                ).fetch();
    }
}
