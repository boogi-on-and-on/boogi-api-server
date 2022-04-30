package boogi.apiserver.domain.alarm.alarmconfig.application;

import boogi.apiserver.domain.alarm.alarmconfig.dao.AlarmConfigRepository;
import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;


@ExtendWith(MockitoExtension.class)
class AlarmConfigCoreServiceTest {

    @Mock
    AlarmConfigRepository alarmConfigRepository;

    @Mock
    UserQueryService userQueryService;

    @InjectMocks
    AlarmConfigCoreService alarmConfigCoreService;

    @Test
    void 알림_없어서_생성() {
        //given
        given(alarmConfigRepository.getAlarmConfigByUserId(anyLong()))
                .willReturn(null);

        User user = User.builder()
                .id(1L)
                .build();
        given(userQueryService.getUser(any()))
                .willReturn(user);

        //when
        AlarmConfig alarmConfig = alarmConfigCoreService.findOrCreateAlarm(user.getId());

        //then
        then(alarmConfigRepository).should(times(1)).save(any());
    }
}