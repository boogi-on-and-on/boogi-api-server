package boogi.apiserver.domain.alarm.alarmconfig.repository;


import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.utils.RepositoryTest;
import boogi.apiserver.utils.fixture.AlarmConfigFixture;
import boogi.apiserver.utils.fixture.UserFixture;
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
            User user = UserFixture.YONGJIN.toUser();
            userRepository.save(user);

            AlarmConfig alarmConfig = AlarmConfigFixture.ALL_FALSE.toAlarmConfig(user);
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
            assertThat(optionalConfig).isEmpty();
        }
    }
}