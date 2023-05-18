package boogi.apiserver.domain.message.message.application;

import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.domain.message.block.repository.MessageBlockRepository;
import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.message.message.dto.response.MessageResponse;
import boogi.apiserver.domain.message.message.dto.response.MessageRoomResponse;
import boogi.apiserver.domain.message.message.repository.MessageRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.global.util.PageableUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

import static boogi.apiserver.utils.fixture.MessageFixture.MESSAGE1;
import static boogi.apiserver.utils.fixture.TimeFixture.STANDARD;
import static boogi.apiserver.utils.fixture.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MessageQueryTest {

    @InjectMocks
    MessageQuery messageQuery;

    @Mock
    MessageBlockRepository messageBlockRepository;

    @Mock
    MessageRepository messageRepository;

    @Mock
    UserRepository userRepository;

    private final User user = SUNDO.toUser(1L);
    private final User receiver = YONGJIN.toUser(2L);

    @Test
    @DisplayName("쪽지 대화방 조회에 성공한다.")
    void getMessageRoomSuccess() {
        User blockedUser = DEOKHWAN.toUser(3L);

        MessageBlock messageBlock = MessageBlock.builder()
                .id(4L)
                .user(user)
                .blockedUser(blockedUser)
                .blocked(true)
                .build();

        Message message = MESSAGE1.toMessage(5L, user, receiver);

        given(messageBlockRepository.findMessageBlocksByUserId(anyLong()))
                .willReturn(List.of(messageBlock));
        given(messageRepository.findMessageWithoutBlockedUser(anyLong(), anyList()))
                .willReturn(List.of(message));
        given(userRepository.findUsersByIds(anyList()))
                .willReturn(List.of(receiver));

        MessageRoomResponse response = messageQuery.getMessageRooms(1L);

        assertThat(response.getMessageRooms()).hasSize(1);

        MessageRoomResponse.MessageRoomDto messageRoomDto = response.getMessageRooms().get(0);
        assertThat(messageRoomDto.getId()).isEqualTo(2L);
        assertThat(messageRoomDto.getName()).isEqualTo(YONGJIN.username);
        assertThat(messageRoomDto.getTagNum()).isEqualTo(YONGJIN.tagNumber);
        assertThat(messageRoomDto.getProfileImageUrl()).isEqualTo(YONGJIN.profileImage);
        assertThat(messageRoomDto.getRecentMessage().getContent()).isEqualTo(MESSAGE1.content);
        assertThat(messageRoomDto.getRecentMessage().getReceivedAt()).isEqualTo(STANDARD);
    }

    @Test
    @DisplayName("상대방 유저와의 대화 목록을 페이지네이션해서 조회한다.")
    void getMessagesByOpponentIdSuccess() {
        Message message = MESSAGE1.toMessage(3L, user, receiver);

        PageRequest pageable = PageRequest.of(0, 1);
        Slice<Message> messages = PageableUtil.getSlice(List.of(message), pageable);

        given(userRepository.findUserById(anyLong())).willReturn(receiver);
        given(messageRepository.findMessages(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(messages);

        MessageResponse response = messageQuery.getMessagesByOpponentId(2L, 1L, pageable);

        assertThat(response.getUser().getId()).isEqualTo(2L);
        assertThat(response.getUser().getName()).isEqualTo(YONGJIN.username);
        assertThat(response.getUser().getTagNum()).isEqualTo(YONGJIN.tagNumber);
        assertThat(response.getUser().getProfileImageUrl()).isEqualTo(YONGJIN.profileImage);

        MessageResponse.MessageDto messageDto = response.getMessages().get(0);
        assertThat(messageDto.getId()).isEqualTo(3L);
        assertThat(messageDto.getContent()).isEqualTo(MESSAGE1.content);
        assertThat(messageDto.getReceivedAt()).isEqualTo(STANDARD);
        assertThat(messageDto.isMe()).isTrue();

        assertThat(response.getPageInfo().getNextPage()).isEqualTo(1);
        assertThat(response.getPageInfo().isHasNext()).isFalse();
    }
}