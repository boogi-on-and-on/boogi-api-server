package boogi.apiserver.domain.alarm.alarmconfig.dao;


import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.builder.TestAlarmConfig;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
        final User user = TestUser.builder().build();
        userRepository.save(user);

        final AlarmConfig alarmConfig = TestAlarmConfig.builder()
                .user(user)
                .build();
        alarmConfigRepository.save(alarmConfig);

        //when
//        AlarmConfig findAlarmConfig = alarmConfigRepository.getAlarmConfigByUserId(user.getId());

        //then
//        assertThat(findAlarmConfig).isEqualTo(alarmConfig);
    }
}