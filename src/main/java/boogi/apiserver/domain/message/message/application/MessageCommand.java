package boogi.apiserver.domain.message.message.application;

import boogi.apiserver.domain.message.block.repository.MessageBlockRepository;
import boogi.apiserver.domain.message.message.repository.MessageRepository;
import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.message.message.dto.request.SendMessageRequest;
import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageCommand {
    private final MessageRepository messageRepository;
    private final MessageBlockRepository messageBlockRepository;
    private final UserRepository userRepository;

    public Long sendMessage(SendMessageRequest request, Long senderId) {
        User sender = userRepository.findUserById(senderId);
        User receiver = userRepository.findUserById(request.getReceiverId());

        boolean isBlockedMessage = messageBlockRepository.existsBlockedFromReceiver(senderId, receiver.getId());

        Message sendedMessage = Message.of(sender, receiver, request.getContent(), isBlockedMessage);
        messageRepository.save(sendedMessage);
        return sendedMessage.getId();
    }
}
