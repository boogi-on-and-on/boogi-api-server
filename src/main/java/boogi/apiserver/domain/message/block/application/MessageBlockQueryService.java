package boogi.apiserver.domain.message.block.application;

import boogi.apiserver.domain.message.block.dao.MessageBlockRepository;
import boogi.apiserver.domain.message.block.dto.response.MessageBlockedUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MessageBlockQueryService {

    private final MessageBlockRepository messageBlockRepository;

    public List<MessageBlockedUserDto> getBlockedMembers(Long userId) {
        return messageBlockRepository.getBlockedUsers(userId);
    }

}
