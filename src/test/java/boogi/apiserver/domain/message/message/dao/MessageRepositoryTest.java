package boogi.apiserver.domain.message.message.dao;

import boogi.apiserver.builder.TestMessage;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.message.block.dao.MessageBlockRepository;
import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.message.message.exception.MessageNotFoundException;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.RepositoryTest;
import boogi.apiserver.utils.TestTimeReflection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageRepositoryTest extends RepositoryTest {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MessageBlockRepository messageBlockRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("차단한 유저를 제외한 모든 유저와의 가장 최근 쪽지를 송신자와 수신자별 각각 1개씩 조회한다.")
    void findMessageWithoutBlockedUser() {
        User user = TestUser.builder().build();
        User blockedUser = TestUser.builder().build();
        User nonBlockedUser = TestUser.builder().build();
        userRepository.saveAll(List.of(user, blockedUser, nonBlockedUser));

        Message blocked_message1 = TestMessage.builder()
                .sender(user)
                .receiver(blockedUser)
                .content("차단된 메시지")
                .blocked_message(true)
                .build();
        Message blocked_message2 = TestMessage.builder()
                .sender(user)
                .receiver(nonBlockedUser)
                .content("차단된 메시지")
                .blocked_message(true)
                .build();
        Message nonblocked_message1 = TestMessage.builder()
                .sender(user)
                .receiver(nonBlockedUser)
                .content("차단되지 않은 메시지1")
                .build();
        TestTimeReflection.setCreatedAt(nonblocked_message1, LocalDateTime.now());
        Message nonblocked_message2 = TestMessage.builder()
                .sender(nonBlockedUser)
                .receiver(user)
                .content("차단되지 않은 메시지2")
                .build();
        TestTimeReflection.setCreatedAt(nonblocked_message2, LocalDateTime.now().minusHours(1));
        messageRepository.saveAll(List.of(blocked_message1, blocked_message2, nonblocked_message1, nonblocked_message2));

        cleanPersistenceContext();

        List<Message> findMessages =
                messageRepository.findMessageWithoutBlockedUser(user.getId(), List.of(blockedUser.getId()));

        assertThat(findMessages).hasSize(2);
        assertThat(findMessages).extracting("id")
                .containsExactly(nonblocked_message1.getId(), nonblocked_message2.getId());
        assertThat(findMessages).extracting("content")
                .containsExactly("차단되지 않은 메시지1", "차단되지 않은 메시지2");
        assertThat(findMessages).extracting("blocked_message")
                .containsOnly(false);
        assertThat(findMessages).extracting("sender").extracting("id")
                .containsExactly(user.getId(), nonBlockedUser.getId());
        assertThat(findMessages).extracting("receiver").extracting("id")
                .containsExactly(nonBlockedUser.getId(), user.getId());
    }

    @Nested
    @DisplayName("ID로 쪽지 조회시")
    class FindMessageById {
        @Test
        @DisplayName("쪽지 조회에 성공한다.")
        void findMessageByIdSuccess() {
            Message message = TestMessage.builder().build();
            messageRepository.save(message);

            cleanPersistenceContext();

            Message findMessage = messageRepository.findMessageById(message.getId());

            assertThat(findMessage).isNotNull();
            assertThat(findMessage.getId()).isEqualTo(message.getId());
        }

        @Test
        @DisplayName("좋아요가 존재하지 않을시 LikeNotFoundException 발생")
        void notExistMessageFail() {
            assertThatThrownBy(() -> messageRepository.findMessageById(1L))
                    .isInstanceOf(MessageNotFoundException.class);
        }
    }

    @Test
    @DisplayName("차단된 메시지를 제외한 나와 상대방과의 1:1 쪽지들을 최신순으로 페이지네이션해서 조회한다.")
    void findMessages() {
        final String MESSAGE_CONTENT = "쪽지 내용입니다.";

        User user = TestUser.builder().build();
        User opponent = TestUser.builder().build();
        userRepository.saveAll(List.of(user, opponent));

        Stream<Message> nonBlockedMessageStream1 = IntStream.range(0, 5)
                .mapToObj(i -> TestMessage.builder()
                        .sender(user)
                        .receiver(opponent)
                        .content(MESSAGE_CONTENT + i)
                        .build());
        Stream<Message> nonBlockedMessageStream2 = IntStream.range(5, 10)
                .mapToObj(i -> TestMessage.builder()
                        .sender(opponent)
                        .receiver(user)
                        .content(MESSAGE_CONTENT + i)
                        .build());
        Stream<Message> blockedMessageStream = IntStream.range(10, 15)
                .mapToObj(i -> TestMessage.builder()
                        .sender(user)
                        .receiver(opponent)
                        .content(MESSAGE_CONTENT + i)
                        .blocked_message(true)
                        .build());
        Stream<Message> nonBlockedMessageStream = Stream.concat(nonBlockedMessageStream1, nonBlockedMessageStream2);
        List<Message> messages = Stream.concat(nonBlockedMessageStream, blockedMessageStream)
                .collect(Collectors.toList());
        messages.forEach(m -> TestTimeReflection.setCreatedAt(m, LocalDateTime.now()));
        messageRepository.saveAll(messages);

        cleanPersistenceContext();

        Pageable pageable = PageRequest.of(0, 10);
        Slice<Message> messagePage = messageRepository.findMessages(opponent.getId(), user.getId(), pageable);

        assertThat(messagePage.hasNext()).isFalse();

        List<Message> findMessages = messagePage.getContent();
        assertThat(findMessages).hasSize(10);

        List<Message> nonBlockedMessages = messages.subList(0, 10);
        Collections.reverse(nonBlockedMessages);

        List<Long> messageIds = nonBlockedMessages.stream()
                .map(Message::getId)
                .collect(Collectors.toList());
        assertThat(findMessages).extracting("id").containsExactlyElementsOf(messageIds);

        List<String> messageContents = nonBlockedMessages.stream()
                .map(Message::getContent)
                .collect(Collectors.toList());
        assertThat(findMessages).extracting("content")
                .containsExactlyElementsOf(messageContents);

        assertThat(findMessages).extracting("sender").extracting("id")
                .containsOnly(user.getId(), opponent.getId());
        assertThat(findMessages).extracting("receiver").extracting("id")
                .containsOnly(user.getId(), opponent.getId());
    }
}