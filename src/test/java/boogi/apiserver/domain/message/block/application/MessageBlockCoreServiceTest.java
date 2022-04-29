package boogi.apiserver.domain.message.block.application;


import boogi.apiserver.domain.message.block.dao.MessageBlockRepository;
import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.global.error.exception.InvalidValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MessageBlockCoreServiceTest {

    @Mock
    MessageBlockRepository messageBlockRepository;

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
}