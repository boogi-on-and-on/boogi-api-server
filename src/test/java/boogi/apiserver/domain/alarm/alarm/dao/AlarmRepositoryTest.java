package boogi.apiserver.domain.alarm.alarm.dao;

import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AlarmRepositoryTest {

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    void 알람목록() {
        //given
        User user1 = User.builder().build();
        userRepository.save(user1);

        Alarm alarm1 = Alarm.builder()
                .body("바디1")
                .head("헤드1")
                .user(user1)
                .build();
        alarm1.setCreatedAt(LocalDateTime.now());

        Alarm alarm2 = Alarm.builder()
                .body("바디2")
                .head("헤드2")
                .user(user1)
                .build();
        alarm2.setCreatedAt(LocalDateTime.now().minusDays(1));

        Alarm alarm3 = Alarm.builder()
                .body("바디3")
                .head("헤드3")
                .user(user1)
                .build();
        alarm3.setCanceledAt(LocalDateTime.now());

        alarmRepository.saveAll(List.of(alarm1, alarm2, alarm3));

        em.flush();
        em.clear();

        //when
        List<Alarm> alarms = alarmRepository.getAlarms(user1.getId());
        assertThat(alarms.size()).isEqualTo(2);

        Alarm first = alarms.get(0);
        Alarm second = alarms.get(1);

        assertThat(first.getId()).isEqualTo(alarm1.getId());
        assertThat(second.getId()).isEqualTo(alarm2.getId());

    }

}