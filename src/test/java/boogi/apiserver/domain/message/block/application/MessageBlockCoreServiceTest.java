package boogi.apiserver.domain.message.block.application;


import boogi.apiserver.domain.message.block.dao.MessageBlockRepository;
import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MessageBlockCoreServiceTest {

    @Mock
    MessageBlockRepository messageBlockRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserQueryService userQueryService;

    @InjectMocks
    MessageBlockCoreService messageBlockCoreService;

    @Test
    void 메시지_이미_차단된_경우() {
        //given
        MessageBlock block = MessageBlock.builder()
                .blocked(false)
                .build();

        given(messageBlockRepository.getMessageBlockByUserId(anyLong(), anyLong()))
                .willReturn(block);

        //then
        assertThatThrownBy(() -> {
            //when
            messageBlockCoreService.releaseUser(anyLong(), anyLong());
        })
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("차단되지 않은 유저입니다.");
    }

    @Test
    void 메시지_차단_해제_성공() {
        //given
        MessageBlock block = MessageBlock.builder().blocked(true).build();

        given(messageBlockRepository.getMessageBlockByUserId(anyLong(), anyLong()))
                .willReturn(block);

        //when
        messageBlockCoreService.releaseUser(anyLong(), anyLong());

        //then
        assertThat(block.getBlocked()).isFalse();
    }

    @Test
    void 메시지_차단하기_페어만_있을때() {
        //given
        User user = User.builder().id(1L).build();
        given(userQueryService.getUser(anyLong()))
                .willReturn(user);

        User blockedUser = User.builder().id(2L).build();
        MessageBlock block = MessageBlock.builder()
                .id(3L)
                .user(user)
                .blockedUser(blockedUser)
                .blocked(false)
                .build();

        given(messageBlockRepository.getMessageBlocksByUserIds(anyLong(), any()))
                .willReturn(List.of(block));

        //when
        messageBlockCoreService.blockUsers(anyLong(), List.of(blockedUser.getId()));

        //then
        then(messageBlockRepository).should(times(1)).updateBulkBlockedStatus(any());

        //todo: 다른 방식으로 테스트하는거 고려
        then(userRepository).should(times(0)).findUsersById(any());
    }

    @Test
    void 메시지_차단하기_페어_없을때() {
        //given
        User user = User.builder().id(1L).build();
        given(userQueryService.getUser(anyLong()))
                .willReturn(user);

        given(messageBlockRepository.getMessageBlocksByUserIds(anyLong(), any()))
                .willReturn(List.of());

        User blockedUser = User.builder().id(2L).build();
        given(userRepository.findUsersById(any()))
                .willReturn(List.of(blockedUser));

        //when
        messageBlockCoreService.blockUsers(user.getId(), List.of(blockedUser.getId()));

        //then
        then(messageBlockRepository).should(times(0)).updateBulkBlockedStatus(any());
        then(userRepository).should(times(1)).findUsersById(any());
        then(messageBlockRepository).should(times(1)).saveAll(any());
    }

}