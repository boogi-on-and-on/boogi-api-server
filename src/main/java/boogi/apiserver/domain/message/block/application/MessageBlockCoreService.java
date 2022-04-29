package boogi.apiserver.domain.message.block.application;

import boogi.apiserver.domain.message.block.dao.MessageBlockRepository;
import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MessageBlockCoreService {

    private final MessageBlockRepository messageBlockRepository;

    @Transactional
    public void releaseUser(Long userId, Long blockedUserId) {
        MessageBlock messageBlock = messageBlockRepository.getMessageBlockByUserId(userId, blockedUserId);

        if (!messageBlock.getBlocked()) {
            throw new InvalidValueException("차단되지 않은 유저입니다.");
        }

        messageBlock.release();
    }
}
