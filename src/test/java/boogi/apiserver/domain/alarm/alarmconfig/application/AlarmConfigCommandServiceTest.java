package boogi.apiserver.domain.alarm.alarmconfig.application;

import boogi.apiserver.builder.TestAlarmConfig;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.alarm.alarmconfig.repository.AlarmConfigRepository;
import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.alarm.alarmconfig.dto.request.AlarmConfigSettingRequest;
import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;


@ExtendWith(MockitoExtension.class)
class AlarmConfigCommandServiceTest {

    @Mock
    AlarmConfigRepository alarmConfigRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AlarmConfigCommandService alarmConfigCommandService;

    @Nested
    @DisplayName("알람 설정 정보 조회")
    class FindOrCreateAlarmConfig {
        @Test
        @DisplayName("알람설정 정보가 없어서 생성한다.")
        void createAlarmConfig() {
            //given
            given(alarmConfigRepository.getAlarmConfigByUserId(anyLong()))
                    .willReturn(Optional.empty());

            final User user = TestUser.builder().id(1L).build();

            given(userRepository.findUserById(anyLong()))
                    .willReturn(user);
            //when
            AlarmConfig config = alarmConfigCommandService.findOrElseCreateAlarmConfig(user.getId());

            //then
            then(userRepository).should(times(1)).findUserById(anyLong());
            then(alarmConfigRepository).should(times(1)).save(any());
        }

        @Test
        @DisplayName("알람설정 정보가 이미 있어서 생성없이 리턴한다.")
        void onlyFindAlarmConfig() {
            //given
            final User user = TestUser.builder()
                    .id(1L)
                    .build();

            final AlarmConfig alarmConfig = TestAlarmConfig.builder()
                    .user(user)
                    .build();

            given(alarmConfigRepository.getAlarmConfigByUserId(anyLong()))
                    .willReturn(Optional.of(alarmConfig));

            //when
            final AlarmConfig config = alarmConfigCommandService.findOrElseCreateAlarmConfig(user.getId());

            //then
            then(userRepository).should(times(0)).findUserById(anyLong());
            then(alarmConfigRepository).should(times(0)).save(any());
        }
    }

    @Test
    @DisplayName("알람 설정을 변경한다.")
    void updateAlarmConfig() {
        //given
        final User user = TestUser.builder().build();
        final AlarmConfig alarmConfig = AlarmConfig.of(user);

        given(alarmConfigRepository.getAlarmConfigByUserId(anyLong()))
                .willReturn(Optional.of(alarmConfig));

        AlarmConfigSettingRequest config = new AlarmConfigSettingRequest(false, true, false, null, true);

        //when
        alarmConfigCommandService.configureAlarm(1L, config);

        //then
        assertThat(alarmConfig.getMessage()).isFalse();
        assertThat(alarmConfig.getNotice()).isTrue();
        assertThat(alarmConfig.getJoinRequest()).isFalse();
        assertThat(alarmConfig.getComment()).isFalse();
        assertThat(alarmConfig.getMention()).isTrue();
    }
}