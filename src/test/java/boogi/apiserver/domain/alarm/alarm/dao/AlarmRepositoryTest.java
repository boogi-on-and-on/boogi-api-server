package boogi.apiserver.domain.alarm.alarm.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CustomDataJpaTest
class AlarmRepositoryTest {

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    @Disabled
    void 알람목록() {
        //given

        User user = TestEmptyEntityGenerator.User();
        userRepository.save(user);

        Alarm alarm = TestEmptyEntityGenerator.Alarm();
        ReflectionTestUtils.setField(alarm, "user", user);

        final Alarm alarm1 = TestEmptyEntityGenerator.Alarm();
        ReflectionTestUtils.setField(alarm1, "head", "해드1");
        ReflectionTestUtils.setField(alarm1, "body", "바디1");
        ReflectionTestUtils.setField(alarm1, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(alarm1, "user", user);


        final Alarm alarm2 = TestEmptyEntityGenerator.Alarm();
        ReflectionTestUtils.setField(alarm2, "head", "해드2");
        ReflectionTestUtils.setField(alarm2, "body", "바디2");
        ReflectionTestUtils.setField(alarm2, "createdAt", LocalDateTime.now().minusDays(1));
        ReflectionTestUtils.setField(alarm2, "user", user);

        final Alarm alarm3 = TestEmptyEntityGenerator.Alarm();
        ReflectionTestUtils.setField(alarm3, "head", "해드3");
        ReflectionTestUtils.setField(alarm3, "body", "바디3");
        ReflectionTestUtils.setField(alarm3, "createdAt", LocalDateTime.now().minusDays(2));
        ReflectionTestUtils.setField(alarm3, "user", user);


        alarmRepository.saveAll(List.of(alarm1, alarm2, alarm3));

        em.flush();
        em.clear();

        //when
        List<Alarm> alarms = alarmRepository.getAlarms(user.getId());

        assertThat(alarms.stream().map(Alarm::getId))
                .containsExactly(alarm1.getId(), alarm2.getId(), alarm3.getId());
    }
}