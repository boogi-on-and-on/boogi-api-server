package boogi.apiserver.domain.alarm.alarm.application;

import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.alarm.alarm.exception.CanNotDeleteAlarmException;
import boogi.apiserver.domain.alarm.alarm.repository.AlarmRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.fixture.AlarmFixture;
import boogi.apiserver.utils.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
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

    private User user;
    private Alarm alarm;

    @BeforeEach
    public void init() {
        this.user = UserFixture.SUNDO.toUser(1L);
        this.alarm = AlarmFixture.ALARM1.toAlarm(2L, user);
    }

    @Nested
    @DisplayName("알림 삭제")
    class DeleteAlarm {
        @Test
        @DisplayName("알림 삭제권한이 없을 때")
        void cannotDeleteAlarm() {
            //given
            given(alarmRepository.findAlarmById(anyLong()))
                    .willReturn(alarm);

            Long otherUserId = 2L;
            //then
            assertThatThrownBy(() -> {
                //when
                alarmCommandService.deleteAlarm(otherUserId, alarm.getId());
            }).isInstanceOf(CanNotDeleteAlarmException.class);
        }

        @Test
        @DisplayName("알림 삭제 성공")
        void success() {
            //given
            given(alarmRepository.findAlarmById(anyLong()))
                    .willReturn(alarm);
            //when
            alarmCommandService.deleteAlarm(user.getId(), alarm.getId());

            //then
            verify(alarmRepository, times(1)).delete(alarm);
        }
    }
}