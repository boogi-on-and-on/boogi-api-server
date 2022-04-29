package boogi.apiserver.domain.message.block.dao;

import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.domain.message.block.dto.MessageBlockedUserDto;

import java.util.List;

public interface MessageBlockRepositoryCustom {
    List<MessageBlockedUserDto> getBlockedUsers(Long userId);

    MessageBlock getMessageBlockByUserId(Long userId, Long blockedUserId);
}
