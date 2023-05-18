package boogi.apiserver.domain.message.block.application;

import boogi.apiserver.domain.message.block.repository.MessageBlockRepository;
import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.domain.message.block.exception.NotBlockedUserException;
import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageBlockCommand {
    private final MessageBlockRepository messageBlockRepository;
    private final UserRepository userRepository;

    public void unblockUser(Long userId, Long blockedUserId) {
        MessageBlock messageBlock = messageBlockRepository.getMessageBlockByUserId(userId, blockedUserId)
                .orElseThrow(NotBlockedUserException::new);

        if (!messageBlock.isBlocked()) {
            throw new NotBlockedUserException();
        }
        messageBlock.unblock();
    }

    public void blockUsers(Long userId, List<Long> blockUserIds) {
        User user = userRepository.findUserById(userId);
        List<MessageBlock> blocks = messageBlockRepository.getMessageBlocksByUserIds(userId, blockUserIds);

        //차단당한 유저, 차단하는 유저 쌍 페어가 이미 있으면 업데이트
        messageBlockRepository.updateBulkBlockedStatus(userId, blockUserIds);

        //로우 페어가 없으면 생성
        insertMessageBlock(blockUserIds, user, blocks);
    }

    private void insertMessageBlock(List<Long> blockUserIds, User user, List<MessageBlock> messageBlocks) {
        Set<Long> messageBlockIds = messageBlocks.stream()
                .map(mb -> mb.getBlockedUser().getId())
                .collect(Collectors.toSet());

        List<Long> newMessageBlockUserIds = blockUserIds.stream()
                .filter(uId -> !messageBlockIds.contains(uId))
                .collect(Collectors.toList());

        List<MessageBlock> newBlocks = userRepository.findUsersByIds(newMessageBlockUserIds).stream()
                .map(blockUser -> MessageBlock.of(user, blockUser))
                .collect(Collectors.toList());

        messageBlockRepository.saveAll(newBlocks);
    }
}

