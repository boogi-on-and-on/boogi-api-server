package boogi.apiserver.domain.alarm.alarmconfig.dao;


import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@CustomDataJpaTest
class AlarmConfigRepositoryTest {

    @Autowired
    AlarmConfigRepository alarmConfigRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void getAlarmConfigByUserId() {
        //given
        final User user = TestEmptyEntityGenerator.User();
        userRepository.save(user);

        final AlarmConfig alarmConfig = TestEmptyEntityGenerator.AlarmConfig();
        ReflectionTestUtils.setField(alarmConfig, "user", user);
        alarmConfigRepository.save(alarmConfig);

        //when
        AlarmConfig findAlarmConfig = alarmConfigRepository.getAlarmConfigByUserId(user.getId());

        //then
        assertThat(findAlarmConfig).isEqualTo(alarmConfig);
    }
}