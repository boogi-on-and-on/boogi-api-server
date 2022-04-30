package boogi.apiserver.domain.alarm.alarmconfig.dao;


import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AlarmConfigRepositoryTest {

    @Autowired
    AlarmConfigRepository alarmConfigRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void getAlarmConfigByUserId() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        AlarmConfig alarmConfig = AlarmConfig.builder()
                .user(user)
                .build();
        alarmConfigRepository.save(alarmConfig);

        //when
        AlarmConfig findAlarmConfig = alarmConfigRepository.getAlarmConfigByUserId(user.getId());

        //then
        assertThat(findAlarmConfig).isEqualTo(alarmConfig);
    }
}