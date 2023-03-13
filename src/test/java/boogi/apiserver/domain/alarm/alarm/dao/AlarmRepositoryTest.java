package boogi.apiserver.domain.alarm.alarm.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.builder.TestAlarm;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.alarm.alarm.exception.AlarmNotFoundException;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.PersistenceUtil;
import boogi.apiserver.utils.TestTimeReflection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CustomDataJpaTest
class AlarmRepositoryTest {

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    EntityManager em;

    PersistenceUtil persistenceUtil;

    @BeforeEach
    void init() {
        persistenceUtil = new PersistenceUtil(em);
    }

    @Test
    void 알람목록() {
        //given

        final User user = TestUser.builder().build();
        userRepository.save(user);

        final Alarm alarm1 = TestAlarm.builder()
                .head("해드1")
                .body("바디1")
                .user(user)
                .build();
        TestTimeReflection.setCreatedAt(alarm1, LocalDateTime.now());

        final Alarm alarm2 = TestAlarm.builder()
                .head("해드2")
                .body("바디2")
                .user(user)
                .build();
        TestTimeReflection.setCreatedAt(alarm2, LocalDateTime.now().minusDays(1));

        final Alarm alarm3 = TestAlarm.builder()
                .head("해드3")
                .body("바디3")
                .user(user)
                .build();
        TestTimeReflection.setCreatedAt(alarm3, LocalDateTime.now().minusDays(2));

        alarmRepository.saveAll(List.of(alarm1, alarm2, alarm3));

        persistenceUtil.cleanPersistenceContext();

        //when
        List<Alarm> alarms = alarmRepository.getAlarms(user.getId());

        assertThat(alarms.stream().map(Alarm::getId))
                .containsExactly(alarm1.getId(), alarm2.getId(), alarm3.getId());
    }

    @Nested
    @DisplayName("findByAlarmId 디폴트 메서드 테스트")
    class findByAlarmId {

        @DisplayName("성공")
        @Test
        void success() {
            final Alarm alarm = TestAlarm.builder().build();
            alarmRepository.save(alarm);

            persistenceUtil.cleanPersistenceContext();

            final Alarm findAlarm = alarmRepository.findByAlarmId(alarm.getId());
            assertThat(findAlarm.getId()).isEqualTo(alarm.getId());
        }

        @DisplayName("throw AlarmNotFoundException")
        @Test
        void throwException() {
            assertThatThrownBy(() -> {
                alarmRepository.findByAlarmId(1L);
            }).isInstanceOf(AlarmNotFoundException.class);
        }
    }
}