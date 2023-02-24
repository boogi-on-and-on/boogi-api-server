package boogi.apiserver.domain.message.block.application;


import boogi.apiserver.builder.TestMessageBlock;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.message.block.dao.MessageBlockRepository;
import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MessageBlockServiceTest {

    @Mock
    MessageBlockRepository messageBlockRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserQueryService userQueryService;

    @InjectMocks
    MessageBlockService messageBlockService;

    @Test
    @DisplayName("메시지 이미 차단한 경우")
    void alreadyBlockMemberMessage() {
        //given
        final MessageBlock block = TestMessageBlock.builder().blocked(false).build();
        given(messageBlockRepository.getMessageBlockByUserId(anyLong(), anyLong()))
                .willReturn(block);

        //expected
        assertThatThrownBy(() -> {
            messageBlockService.releaseUser(anyLong(), anyLong());
        })
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("차단되지 않은 유저입니다.");
    }

    @Test
    @DisplayName("메시지 차단 해제 성공")
    void unblockMemberMessage() {
        //given
        final MessageBlock block = TestMessageBlock.builder().blocked(true).build();
        given(messageBlockRepository.getMessageBlockByUserId(anyLong(), anyLong()))
                .willReturn(block);

        //when
        messageBlockService.releaseUser(anyLong(), anyLong());

        //then
        assertThat(block.getBlocked()).isFalse();
    }

    @Test
    @DisplayName("row 업데이트만 진행하는 경우")
    void updateMessageBlock() {
        //given
        final User user = TestUser.builder().id(1L).build();
        given(userRepository.findByUserId(anyLong()))
                .willReturn(user);

        final User blockedUser = TestUser.builder().id(2L).build();

        final MessageBlock block = TestMessageBlock.builder()
                .id(3L)
                .user(user)
                .blockedUser(blockedUser)
                .blocked(false)
                .build();
        given(messageBlockRepository.getMessageBlocksByUserIds(anyLong(), any()))
                .willReturn(List.of(block));

        //when
        messageBlockService.blockUsers(anyLong(), List.of(blockedUser.getId()));

        //then
        then(messageBlockRepository).should(times(1)).updateBulkBlockedStatus(any());

        //todo: 다른 방식으로 테스트하는거 고려
        then(userRepository).should(times(0)).findUsersByIds(any());
    }

    @Test
    @DisplayName("차단하기 페어 추가")
    void insertMessageBlock() {
        //given
        final User user = TestUser.builder().id(1L).build();
        given(userRepository.findByUserId(anyLong()))
                .willReturn(user);

        given(messageBlockRepository.getMessageBlocksByUserIds(anyLong(), any()))
                .willReturn(List.of());

        final User blockedUser = TestUser.builder().id(2L).build();
        given(userRepository.findUsersByIds(any()))
                .willReturn(List.of(blockedUser));

        //when
        messageBlockService.blockUsers(user.getId(), List.of(blockedUser.getId()));

        //then
        then(messageBlockRepository).should(times(0)).updateBulkBlockedStatus(any());
        then(userRepository).should(times(1)).findUsersByIds(any());
        then(messageBlockRepository).should(times(1)).saveAll(any());
    }
}