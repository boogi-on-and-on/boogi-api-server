package boogi.apiserver.domain.alarm.alarm.application;

import boogi.apiserver.builder.TestAlarm;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.alarm.alarm.repository.AlarmRepository;
import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.alarm.alarm.exception.CanNotDeleteAlarmException;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Nested
    @DisplayName("알림 삭제")
    class DeleteAlarm {
        @Test
        @DisplayName("알림 삭제권한이 없을 때")
        void cannotDeleteAlarm() {
            //given
            final User user = TestUser.builder().id(1L).build();

            final Alarm alarm = TestAlarm.builder().user(user).build();

            given(alarmRepository.findAlarmById(anyLong()))
                    .willReturn(alarm);

            //then
            assertThatThrownBy(() -> {
                //when
                alarmCommandService.deleteAlarm(2L, 1L);
            }).isInstanceOf(CanNotDeleteAlarmException.class);
        }

        @Test
        @DisplayName("알림 삭제 성공")
        void success() {
            //given
            final User user = TestUser.builder().id(1L).build();
            final Alarm alarm = TestAlarm.builder().user(user).build();

            given(alarmRepository.findAlarmById(anyLong()))
                    .willReturn(alarm);
            //when
            alarmCommandService.deleteAlarm(user.getId(), 1L);

            //then
            verify(alarmRepository, times(1)).delete(alarm);
        }
    }
}