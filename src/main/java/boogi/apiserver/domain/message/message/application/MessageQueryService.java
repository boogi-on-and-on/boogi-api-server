package boogi.apiserver.domain.message.message.application;

import boogi.apiserver.domain.message.block.dao.MessageBlockRepository;
import boogi.apiserver.domain.message.message.dao.MessageRepository;
import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.message.message.dto.response.MessageResponse;
import boogi.apiserver.domain.message.message.dto.response.MessageRoomResponse;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageQueryService {
    private final MessageBlockRepository messageBlockRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageRoomResponse getMessageRooms(Long userId) {
        List<Long> blockedUserIds = getBlockedUserIds(userId);
        List<Message> messages = messageRepository.findMessageByUserIdWithoutBlockedUser(userId, blockedUserIds);

        // 나와 상대방의 대화 중 가장 최근 대화 1개씩 추출 -> 순서유지를 위해 LinkedHashMap 사용
        LinkedHashMap<Long, Message> dedupMessages = messages.stream()
                .collect(Collectors.toMap(
                        m1 -> getOpponentId(userId, m1),
                        m2 -> m2,
                        (o, n) -> o,
                        LinkedHashMap::new
                ));
        List<Long> opponentIds = new ArrayList<>(dedupMessages.keySet());

        Map<Long, User> opponentUserMap = userRepository.findUsersByIds(opponentIds).stream()
                .collect(Collectors.toMap(
                        u1 -> u1.getId(),
                        u2 -> u2,
                        (o, n) -> o,
                        HashMap::new
                ));

        return MessageRoomResponse.of(opponentIds, opponentUserMap, dedupMessages);
    }

    public MessageResponse getMessagesByOpponentId(Long opponentId, Long userId, Pageable pageable) {
        User opponentUser = userRepository.findByUserId(opponentId);
        Slice<Message> messages = messageRepository.findMessages(opponentId, userId, pageable);

        return MessageResponse.of(opponentUser, messages, userId);
    }

    private Long getOpponentId(Long userId, Message m1) {
        Long senderId = m1.getSender().getId();
        Long receiverId = m1.getReceiver().getId();
        return (senderId.equals(userId) ? receiverId : senderId);
    }

    private List<Long> getBlockedUserIds(Long userId) {
        List<Long> blockedUserIds = messageBlockRepository.findMessageBlocksByUserId(userId).stream()
                .map(mb -> mb.getBlockedUser().getId())
                .collect(Collectors.toList());

        // native SQL의 not in에 null 입력으로 인한 에러 처리
        if (blockedUserIds.size() <= 0) {
            blockedUserIds.add(Long.valueOf(0l));
        }
        return blockedUserIds;
    }
}
