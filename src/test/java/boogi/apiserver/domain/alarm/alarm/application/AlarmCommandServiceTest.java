package boogi.apiserver.domain.alarm.alarm.application;

import boogi.apiserver.builder.TestAlarm;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.alarm.alarm.dao.AlarmRepository;
import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.alarm.alarm.exception.CanNotDeleteAlarmException;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AlarmCommandServiceTest {

    @InjectMocks
    AlarmCommandService alarmCommandService;

    @Mock
    AlarmRepository alarmRepository;

    @Test
    void 알림_삭제권한_없을때() {
        //given
        final User user = TestUser.builder().id(1L).build();

        final Alarm alarm = TestAlarm.builder().user(user).build();

        given(alarmRepository.findByAlarmId(anyLong()))
                .willReturn(alarm);

        //then
        assertThatThrownBy(() -> {
            //when
            alarmCommandService.deleteAlarm(2L, anyLong());
        }).isInstanceOf(CanNotDeleteAlarmException.class);
    }

    @Test
    @Disabled
    void 알림_삭제성공() {
        //given
        final User user = TestUser.builder().id(1L).build();
        final Alarm alarm = TestAlarm.builder().user(user).build();

        given(alarmRepository.findByAlarmId(anyLong()))
                .willReturn(alarm);

        //when
        alarmCommandService.deleteAlarm(user.getId(), anyLong());

        //then
        verify(alarmRepository, times(1)).delete(alarm);
    }
}