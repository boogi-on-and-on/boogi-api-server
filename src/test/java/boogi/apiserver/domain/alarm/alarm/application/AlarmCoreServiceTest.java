package boogi.apiserver.domain.alarm.alarm.application;

import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import org.junit.jupiter.api.Disabled;
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
class AlarmCoreServiceTest {

    @Mock
    AlarmQueryService alarmQueryService;

    @InjectMocks
    AlarmCoreService alarmCoreService;

    @Test
    void 알림_삭제권한_없을때() {
        //given
        Alarm alarm = Alarm.builder()
                .user(User.builder().id(1L).build())
                .build();

        given(alarmQueryService.getAlarm(anyLong()))
                .willReturn(alarm);

        //then
        assertThatThrownBy(() -> {
            //when
            alarmCoreService.deleteAlarm(2L, anyLong());
        })
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("해당 알림을 삭제할 권한이 없습니다.");
    }

    @Test
    @Disabled
    void 알림_삭제성공() {
        //given
        Alarm alarm = Alarm.builder()
                .user(User.builder().id(1L).build())
                .build();

        given(alarmQueryService.getAlarm(anyLong()))
                .willReturn(alarm);

        //when
        alarmCoreService.deleteAlarm(1L, anyLong());

        //then
//        assertThat(alarm.getCanceledAt()).isNotNull();
    }
}