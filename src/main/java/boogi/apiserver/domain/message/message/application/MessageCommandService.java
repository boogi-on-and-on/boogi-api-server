package boogi.apiserver.domain.message.message.application;

import boogi.apiserver.domain.message.block.dao.MessageBlockRepository;
import boogi.apiserver.domain.message.message.dao.MessageRepository;
import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.message.message.dto.request.SendMessageRequest;
import boogi.apiserver.domain.message.message.dto.response.MessageResponse;
import boogi.apiserver.domain.message.message.dto.response.MessageRoomResponse;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageCommandService {
    private final MessageRepository messageRepository;
    private final MessageBlockRepository messageBlockRepository;
    private final UserRepository userRepository;

    public Long sendMessage(SendMessageRequest request, Long senderId) {
        User sender = userRepository.findByUserId(senderId);
        User receiver = userRepository.findByUserId(request.getReceiverId());

        boolean isBlockedMessage = messageBlockRepository.existsBlockedFromReceiver(senderId, receiver.getId());

        Message sendedMessage = Message.of(sender, receiver, request.getContent(), isBlockedMessage);
        messageRepository.save(sendedMessage);
        return sendedMessage.getId();
    }
}
