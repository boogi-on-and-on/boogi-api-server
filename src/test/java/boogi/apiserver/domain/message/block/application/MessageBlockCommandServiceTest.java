package boogi.apiserver.domain.message.block.application;


import boogi.apiserver.builder.TestMessageBlock;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.message.block.dao.MessageBlockRepository;
import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.domain.message.block.exception.NotBlockedUserException;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageBlockCommandServiceTest {
    @InjectMocks
    MessageBlockCommandService messageBlockCommandService;

    @Mock
    MessageBlockRepository messageBlockRepository;

    @Mock
    UserRepository userRepository;


    @Nested
    @DisplayName("쪽지 차단을 해제할시")
    class UnblockUserTest {
        @Test
        @DisplayName("차단되지 않은 유저에게 차단 해제시 NotBlockedUserException 예외가 발생한다.")
        void unblockToNotBlockedUserFail() {
            //given
            final MessageBlock block = TestMessageBlock.builder().blocked(false).build();
            given(messageBlockRepository.getMessageBlockByUserId(anyLong(), anyLong()))
                    .willReturn(Optional.of(block));

            //expected
            assertThatThrownBy(() -> messageBlockCommandService.unblockUser(1L, 1L))
                    .isInstanceOf(NotBlockedUserException.class);
        }

        @Test
        @DisplayName("쪽지 차단이 해제된다.")
        void unblockMemberMessage() {
            //given
            final MessageBlock block = TestMessageBlock.builder().blocked(true).build();
            given(messageBlockRepository.getMessageBlockByUserId(anyLong(), anyLong()))
                    .willReturn(Optional.of(block));

            //when
            messageBlockCommandService.unblockUser(1L, 1L);

            //then
            assertThat(block.isBlocked()).isFalse();
        }
    }

    @Nested
    @DisplayName("쪽지 차단할시")
    class BlockUserTest {

        @Captor
        ArgumentCaptor<List<MessageBlock>> messageBlocksCaptor;

        @Test
        @DisplayName("이미 쪽지 차단 엔티티가 존재하는 경우 해당 엔티티를 업데이트하고, 존재하지 않으면 새로 생성한다.")
        void blockeUsersSuccess() {
            //given
            final User user = TestUser.builder().id(1L).build();
            given(userRepository.findUserById(anyLong()))
                    .willReturn(user);

            final User blockedUser = TestUser.builder().id(2L).build();

            final MessageBlock block = TestMessageBlock.builder()
                    .id(3L)
                    .user(user)
                    .blockedUser(blockedUser)
                    .blocked(false)
                    .build();
            given(messageBlockRepository.getMessageBlocksByUserIds(anyLong(), anyList()))
                    .willReturn(List.of(block));

            given(userRepository.findUsersByIds(anyList()))
                    .willReturn(List.of(blockedUser));

            //when
            messageBlockCommandService.blockUsers(1L, List.of(blockedUser.getId()));

            //then
            verify(messageBlockRepository, times(1))
                    .updateBulkBlockedStatus(anyLong(), anyList());
            verify(messageBlockRepository, times(1)).saveAll(messageBlocksCaptor.capture());

            List<MessageBlock> messageBlocks = messageBlocksCaptor.getValue();

            assertThat(messageBlocks.size()).isEqualTo(1);
            MessageBlock newMessageBlock = messageBlocks.get(0);
            assertThat(newMessageBlock.getUser().getId()).isEqualTo(1L);
            assertThat(newMessageBlock.getBlockedUser().getId()).isEqualTo(2L);
            assertThat(newMessageBlock.isBlocked()).isTrue();
        }
    }
}