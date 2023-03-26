package boogi.apiserver.domain.message.message.application;

import boogi.apiserver.builder.TestMessage;
import boogi.apiserver.builder.TestMessageBlock;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.message.block.dao.MessageBlockRepository;
import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.domain.message.message.dao.MessageRepository;
import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.message.message.dto.response.MessageResponse;
import boogi.apiserver.domain.message.message.dto.response.MessageRoomResponse;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.util.PageableUtil;
import boogi.apiserver.utils.TestTimeReflection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MessageQueryServiceTest {

    @InjectMocks
    MessageQueryService messageQueryService;

    @Mock
    MessageBlockRepository messageBlockRepository;

    @Mock
    MessageRepository messageRepository;

    @Mock
    UserRepository userRepository;

    @Test
    @DisplayName("쪽지 대화방 조회에 성공한다.")
    void getMessageRoomSuccess() {
        User user = TestUser.builder().id(1L).build();
        User receiver = TestUser.builder()
                .id(2L)
                .username("수신유저")
                .tagNumber("#0001")
                .profileImageUrl("url")
                .build();
        User blockedUser = TestUser.builder().id(3L).build();

        MessageBlock messageBlock = TestMessageBlock.builder()
                .id(3L)
                .blockedUser(blockedUser)
                .blocked(true)
                .build();

        LocalDateTime now = LocalDateTime.now();

        Message message = TestMessage.builder()
                .id(4L)
                .content("쪽지")
                .sender(user)
                .receiver(receiver)
                .build();
        TestTimeReflection.setCreatedAt(message, now);

        given(messageBlockRepository.findMessageBlocksByUserId(anyLong()))
                .willReturn(List.of(messageBlock));
        given(messageRepository.findMessageWithoutBlockedUser(anyLong(), anyList()))
                .willReturn(List.of(message));
        given(userRepository.findUsersByIds(anyList()))
                .willReturn(List.of(receiver));

        MessageRoomResponse response = messageQueryService.getMessageRooms(1L);

        assertThat(response.getMessageRooms()).hasSize(1);

        MessageRoomResponse.MessageRoomDto messageRoomDto = response.getMessageRooms().get(0);
        assertThat(messageRoomDto.getId()).isEqualTo(2L);
        assertThat(messageRoomDto.getName()).isEqualTo("수신유저");
        assertThat(messageRoomDto.getTagNum()).isEqualTo("#0001");
        assertThat(messageRoomDto.getProfileImageUrl()).isEqualTo("url");
        assertThat(messageRoomDto.getRecentMessage().getContent()).isEqualTo("쪽지");
        assertThat(messageRoomDto.getRecentMessage().getReceivedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("상대방 유저와의 대화 목록을 페이지네이션해서 조회한다.")
    void getMessagesByOpponentIdSuccess() {
        User user = TestUser.builder()
                .id(1L)
                .username("유저")
                .tagNumber("#0001")
                .profileImageUrl("url1")
                .build();
        User opponent = TestUser.builder()
                .id(2L)
                .username("상대방유저")
                .tagNumber("#0002")
                .profileImageUrl("url2")
                .build();

        Message message = TestMessage.builder()
                .id(3L)
                .sender(user)
                .receiver(opponent)
                .content("쪽지")
                .build();
        LocalDateTime now = LocalDateTime.now();
        TestTimeReflection.setCreatedAt(message, now);

        PageRequest pageable = PageRequest.of(0, 1);
        Slice<Message> messages = PageableUtil.getSlice(List.of(message), pageable);

        given(userRepository.findByUserId(anyLong())).willReturn(opponent);
        given(messageRepository.findMessages(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(messages);

        MessageResponse response = messageQueryService.getMessagesByOpponentId(2L, 1L, pageable);

        assertThat(response.getUser().getId()).isEqualTo(2L);
        assertThat(response.getUser().getName()).isEqualTo("상대방유저");
        assertThat(response.getUser().getTagNum()).isEqualTo("#0002");
        assertThat(response.getUser().getProfileImageUrl()).isEqualTo("url2");

        MessageResponse.MessageDto messageDto = response.getMessages().get(0);
        assertThat(messageDto.getId()).isEqualTo(3L);
        assertThat(messageDto.getContent()).isEqualTo("쪽지");
        assertThat(messageDto.getReceivedAt()).isEqualTo(now);
        assertThat(messageDto.isMe()).isTrue();

        assertThat(response.getPageInfo().getNextPage()).isEqualTo(1);
        assertThat(response.getPageInfo().isHasNext()).isFalse();
    }
}