package boogi.apiserver.domain.message.block.application;

import boogi.apiserver.domain.message.block.dao.MessageBlockRepository;
import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MessageBlockCoreService {

    private final MessageBlockRepository messageBlockRepository;
    private final UserRepository userRepository;

    private final UserQueryService userQueryService;

    @Transactional
    public void releaseUser(Long userId, Long blockedUserId) {
        MessageBlock messageBlock = messageBlockRepository.getMessageBlockByUserId(userId, blockedUserId);

        if (!messageBlock.getBlocked()) {
            throw new InvalidValueException("차단되지 않은 유저입니다.");
        }

        messageBlock.release();
    }

    @Transactional
    public void blockUsers(Long userId, List<Long> blockUserIds) {
        User user = userQueryService.getUser(userId);
        List<MessageBlock> blocks = messageBlockRepository.getMessageBlocksByUserIds(userId, blockUserIds);

        //차단당한 유저, 차단하는 유저 쌍 페어가 이미 있으면 업데이트
        updatePreviousExistsMessageBlock(blockUserIds, blocks);

        //로우 페어가 없으면 생성
        insertMessageBlock(blockUserIds, user, blocks);
    }

    private void insertMessageBlock(List<Long> blockUserIds, User user, List<MessageBlock> messageBlocks) {
        Map<Long, MessageBlock> messageBlockMap = messageBlocks.stream()
                .collect(Collectors.toMap(m -> m.getBlockedUser().getId(), m -> m));
        List<Long> newMessageBlockUserIds = blockUserIds.stream()
                .filter(uId -> !messageBlockMap.containsKey(uId))
                .collect(Collectors.toList());

        if (newMessageBlockUserIds.size() == 0) {
            return;
        }

        List<MessageBlock> newBlocks = userRepository.findUsersById(newMessageBlockUserIds).stream()
                .map(blockUser -> MessageBlock.of(user, blockUser))
                .collect(Collectors.toList());
        messageBlockRepository.saveAll(newBlocks);
    }

    private void updatePreviousExistsMessageBlock(List<Long> blockUserIds, List<MessageBlock> blocks) {
        blocks.forEach(MessageBlock::block);

        List<Long> blockIds = blocks.stream()
                .map(MessageBlock::getId)
                .collect(Collectors.toList());

        if (blockIds.size() == 0) {
            return;
        }

        messageBlockRepository.updateBulkBlockedStatus(blockUserIds);
    }
}
