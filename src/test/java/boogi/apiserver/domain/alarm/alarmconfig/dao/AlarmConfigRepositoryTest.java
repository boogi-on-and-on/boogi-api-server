package boogi.apiserver.domain.alarm.alarmconfig.dao;


import boogi.apiserver.builder.TestAlarmConfig;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.RepositoryTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AlarmConfigRepositoryTest extends RepositoryTest {

    @Autowired
    AlarmConfigRepository alarmConfigRepository;

    @Autowired
    UserRepository userRepository;

    @Nested
    @DisplayName("userId로 알람을 조회")
    class GetAlarmConfigByUserId {
        @Test
        @DisplayName("목록에 있는 경우 로우를 Optional에 넣어서 리턴한다.")
        void optionalAlarmConfig() {
            //given
            final User user = TestUser.builder().build();
            userRepository.save(user);

            final AlarmConfig alarmConfig = TestAlarmConfig.builder()
                    .user(user)
                    .build();
            alarmConfigRepository.save(alarmConfig);

            cleanPersistenceContext();

            //when
            final Optional<AlarmConfig> optionalConfig = alarmConfigRepository.getAlarmConfigByUserId(user.getId());

            //then
            final AlarmConfig findConfig = optionalConfig.get();
            assertThat(findConfig.getId()).isEqualTo(alarmConfig.getId());
        }

        @Test
        @DisplayName("목록에 없어서 Optional.empty() 리턴")
        void emptyAlarmConfig() {
            //when
            final Optional<AlarmConfig> optionalConfig = alarmConfigRepository.getAlarmConfigByUserId(1L);

            //then
            assertThat(optionalConfig.isEmpty()).isTrue();
        }
    }
}