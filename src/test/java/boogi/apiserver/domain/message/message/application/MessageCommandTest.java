package boogi.apiserver.domain.message.message.application;

import boogi.apiserver.domain.message.block.repository.MessageBlockRepository;
import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.message.message.dto.request.SendMessageRequest;
import boogi.apiserver.domain.message.message.repository.MessageRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static boogi.apiserver.utils.fixture.UserFixture.SUNDO;
import static boogi.apiserver.utils.fixture.UserFixture.YONGJIN;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class MessageCommandTest {

    @InjectMocks
    MessageCommand messageCommand;

    @Mock
    MessageRepository messageRepository;

    @Mock
    MessageBlockRepository messageBlockRepository;

    @Mock
    UserRepository userRepository;

    @Captor
    ArgumentCaptor<Message> messageCaptor;

    @Test
    @DisplayName("메시지 보내기에 성공한다.")
    void sendMessageSuccess() {
        final String MESSAGE_CONTENT = "쪽지";
        final long SENDER_ID = 1L;
        final long RECEIVER_ID = 2L;

        User sender = SUNDO.toUser(SENDER_ID);
        User receiver = YONGJIN.toUser(RECEIVER_ID);

        SendMessageRequest request = new SendMessageRequest(RECEIVER_ID, MESSAGE_CONTENT);

        given(userRepository.findUserById(eq(SENDER_ID))).willReturn(sender);
        given(userRepository.findUserById(eq(RECEIVER_ID))).willReturn(receiver);
        given(messageBlockRepository.existsBlockedFromReceiver(anyLong(), anyLong()))
                .willReturn(false);

        messageCommand.sendMessage(request, SENDER_ID);

        verify(messageRepository, times(1)).save(messageCaptor.capture());

        Message newMessage = messageCaptor.getValue();
        assertThat(newMessage.getContent()).isEqualTo(MESSAGE_CONTENT);
        assertThat(newMessage.getSender().getId()).isEqualTo(SENDER_ID);
        assertThat(newMessage.getReceiver().getId()).isEqualTo(RECEIVER_ID);
        assertThat(newMessage.isBlocked_message()).isFalse();
    }
}