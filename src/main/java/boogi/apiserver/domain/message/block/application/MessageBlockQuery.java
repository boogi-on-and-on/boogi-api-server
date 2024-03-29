package boogi.apiserver.domain.message.block.application;

import boogi.apiserver.domain.message.block.repository.MessageBlockRepository;
import boogi.apiserver.domain.message.block.dto.dto.MessageBlockedUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MessageBlockQuery {

    private final MessageBlockRepository messageBlockRepository;

    public List<MessageBlockedUserDto> getBlockedUsers(Long userId) {
        return messageBlockRepository.getBlockedUsers(userId);
    }
}
