package boogi.apiserver.domain.message.message.application;

import boogi.apiserver.domain.message.block.dao.MessageBlockRepository;
import boogi.apiserver.domain.message.message.dao.MessageRepository;
import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.message.message.dto.request.SendMessage;
import boogi.apiserver.domain.message.message.dto.response.MessageResponse;
import boogi.apiserver.domain.message.message.dto.response.MessageRoomResponse;
import boogi.apiserver.domain.user.application.UserQueryService;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MessageService {

    private final UserQueryService userQueryService;

    private final MessageRepository messageRepository;
    private final MessageBlockRepository messageBlockRepository;
    private final UserRepository userRepository;

    @Transactional
    public Message sendMessage(SendMessage sendMessage, Long senderId) {
        User sender = userQueryService.getUser(senderId);
        User receiver = userQueryService.getUser(sendMessage.getReceiverId());

        Boolean isBlockedMessage = (messageBlockRepository.checkOnlyReceiverBlockedFromSender(senderId, receiver.getId()))
                ? Boolean.TRUE : Boolean.FALSE;

        Message sendedMessage = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(sendMessage.getContent())
                .blocked_message(isBlockedMessage)
                .build();

        return messageRepository.save(sendedMessage);
    }

    public MessageRoomResponse getMessageRooms(Long userId) {
        List<Long> blockedUserIds = messageBlockRepository.findMessageBlocksByUserId(userId).stream()
                .map(mb -> mb.getBlockedUser().getId())
                .collect(Collectors.toList());

        // native SQL의 not in에 null 입력으로 인한 에러 처리
        if (blockedUserIds.size() <= 0)
            blockedUserIds.add(Long.valueOf(0l));

        List<Message> messages = messageRepository.findMessageByUserIdWithoutBlockedUser(userId, blockedUserIds);

        // 나와 상대방의 대화 중 가장 최근 대화 1개씩 추출 -> 순서유지를 위해 LinkedHashMap 사용
        LinkedHashMap<Long, Message> dedupMessages = messages.stream()
                .collect(Collectors.toMap(
                        m1 -> {
                            Long senderId = m1.getSender().getId();
                            Long receiverId = m1.getReceiver().getId();
                            return (senderId.equals(userId) ? receiverId : senderId);
                        },
                        m2 -> m2,
                        (o, n) -> o,
                        LinkedHashMap::new
                ));
        List<Long> opponentIds = dedupMessages.keySet().stream()
                .collect(Collectors.toList());

        Map<Long, User> opponentUserMap = userRepository.findUsersByIds(opponentIds).stream()
                .collect(Collectors.toMap(
                        u1 -> u1.getId(),
                        u2 -> u2,
                        (o, n) -> o,
                        HashMap::new
                ));

        List<MessageRoomResponse.MessageRoom> messageRooms = opponentIds.stream()
                .map(oid ->
                        MessageRoomResponse.MessageRoom
                                .toDto(opponentUserMap.get(oid), dedupMessages.get(oid)))
                .collect(Collectors.toList());

        return MessageRoomResponse.of(messageRooms);
    }

    public MessageResponse getMessagesByOpponentId(Long opponentId, Long userId, Pageable pageable) {
        User opponentUser = userQueryService.getUser(opponentId);
        Slice<Message> messages = messageRepository.findMessagesByOpponentIdAndMyId(opponentId, userId, pageable);

        return MessageResponse.of(opponentUser, messages, userId);
    }
}
