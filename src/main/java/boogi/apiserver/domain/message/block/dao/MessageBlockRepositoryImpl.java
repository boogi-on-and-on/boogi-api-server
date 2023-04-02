package boogi.apiserver.domain.message.block.dao;

import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.domain.message.block.dto.dto.MessageBlockedUserDto;
import boogi.apiserver.domain.message.block.dto.dto.QMessageBlockedUserDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static boogi.apiserver.domain.message.block.domain.QMessageBlock.messageBlock;


@RequiredArgsConstructor
public class MessageBlockRepositoryImpl implements MessageBlockRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MessageBlockedUserDto> getBlockedUsers(Long userId) {
        return queryFactory.select(new QMessageBlockedUserDto(
                        messageBlock.blockedUser.id,
                        messageBlock.blockedUser.username.value,
                        messageBlock.blockedUser.tagNumber.value
                ))
                .from(messageBlock)
                .where(
                        messageBlock.user.id.eq(userId),
                        messageBlock.blocked.eq(true)
                )
                .fetch();
    }

    @Override
    public Optional<MessageBlock> getMessageBlockByUserId(Long userId, Long blockedUserId) {
        MessageBlock findMessageBlock = queryFactory.selectFrom(messageBlock)
                .where(
                        messageBlock.user.id.eq(userId),
                        messageBlock.blockedUser.id.eq(blockedUserId)
                )
                .fetchOne();

        return Optional.ofNullable(findMessageBlock);
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
    public void updateBulkBlockedStatus(Long userId, List<Long> blockUserIds) {
        if (blockUserIds.isEmpty()) {
            return;
        }

        queryFactory.update(messageBlock)
                .set(messageBlock.blocked, true)
                .where(messageBlock.user.id.eq(userId),
                        messageBlock.blockedUser.id.in(blockUserIds))
                .execute();
    }

    @Override
    public boolean existsBlockedFromReceiver(Long senderId, Long receiverId) {
        MessageBlock findMessageBlock = queryFactory.selectFrom(messageBlock)
                .where(
                        messageBlock.user.id.eq(senderId),
                        messageBlock.blockedUser.id.eq(receiverId),
                        messageBlock.blocked.isTrue()
                ).fetchFirst();

        return findMessageBlock != null;
    }

    // todo: getBlockedUsers와 중복
    @Override
    public List<MessageBlock> findMessageBlocksByUserId(Long userId) {
        return queryFactory.selectFrom(messageBlock)
                .where(
                        messageBlock.user.id.eq(userId),
                        messageBlock.blocked.isTrue()
                ).fetch();
    }
}
