package boogi.apiserver.domain.alarm.alarmconfig.application;

import boogi.apiserver.domain.alarm.alarmconfig.dao.AlarmConfigRepository;
import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.alarm.alarmconfig.dto.request.AlarmConfigSettingRequest;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
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

        final User user = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userQueryService.getUser(any()))
                .willReturn(user);
        //when
        AlarmConfig alarmConfig = alarmConfigCoreService.findOrElseCreateAlarmConfig(user.getId());

        //then
        then(alarmConfigRepository).should(times(1)).save(any());
    }

    @Test
    void 알림_설정_변경() {
        //given
        final User user = TestEmptyEntityGenerator.User();
        AlarmConfig alarmConfig = AlarmConfig.of(user);

        given(alarmConfigRepository.getAlarmConfigByUserId(anyLong()))
                .willReturn(alarmConfig);

        AlarmConfigSettingRequest config = AlarmConfigSettingRequest.builder()
                .message(false)
                .notice(true)
                .join(false)
                .comment(false)
                .mention(false)
                .build();

        //when
        alarmConfigCoreService.configureAlarm(anyLong(), config);

        //then
        assertThat(alarmConfig.getMessage()).isFalse();
        assertThat(alarmConfig.getNotice()).isTrue();
        assertThat(alarmConfig.getJoinRequest()).isFalse();
        assertThat(alarmConfig.getComment()).isFalse();
        assertThat(alarmConfig.getMention()).isFalse();
    }
}