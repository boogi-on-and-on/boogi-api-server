package boogi.apiserver.domain.alarm.alarm.application;

import boogi.apiserver.domain.alarm.alarm.dao.AlarmRepository;
import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AlarmServiceTest {

    @Mock
    AlarmQueryService alarmQueryService;

    @InjectMocks
    AlarmService alarmService;

    @Mock
    AlarmRepository alarmRepository;

    @Test
    void 알림_삭제권한_없을때() {
        //given
        User user = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user, "id", 1L);

        Alarm alarm = TestEmptyEntityGenerator.Alarm();
        ReflectionTestUtils.setField(alarm, "user", user);

        given(alarmQueryService.getAlarm(anyLong()))
                .willReturn(alarm);

        //then
        assertThatThrownBy(() -> {
            //when
            alarmService.deleteAlarm(2L, anyLong());
        })
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("해당 알림을 삭제할 권한이 없습니다.");
    }

    @Test
    @Disabled
    void 알림_삭제성공() {
        //given
        User user = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user, "id", 1L);

        Alarm alarm = TestEmptyEntityGenerator.Alarm();
        ReflectionTestUtils.setField(alarm, "user", user);

        given(alarmQueryService.getAlarm(anyLong()))
                .willReturn(alarm);

        //when
        alarmService.deleteAlarm(1L, anyLong());

        //then
        verify(alarmRepository, times(1)).delete(any());
    }
}