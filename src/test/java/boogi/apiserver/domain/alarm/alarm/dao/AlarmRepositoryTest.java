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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Nested
    @DisplayName("알람 ID로 알람 조회")
    class findAlarmById {

        @DisplayName("성공")
        @Test
        void success() {
            final Alarm alarm = TestAlarm.builder().build();
            alarmRepository.save(alarm);

            persistenceUtil.cleanPersistenceContext();

            final Alarm findAlarm = alarmRepository.findAlarmById(alarm.getId());
            assertThat(findAlarm.getId()).isEqualTo(alarm.getId());
        }

        @DisplayName("throw AlarmNotFoundException")
        @Test
        void throwException() {
            assertThatThrownBy(() -> {
                alarmRepository.findAlarmById(1L);
            }).isInstanceOf(AlarmNotFoundException.class);
        }
    }

    @Test
    @DisplayName("유저 ID로 알람을 최신순으로 조회한다.")
    void getAlarms() {
        final String ALARM_HEAD = "헤드";
        final String ALARM_BODY = "바디";

        //given
        final User user = TestUser.builder().build();
        userRepository.save(user);

        List<Alarm> alarms = IntStream.range(0, 3)
                .mapToObj(i -> {
                    Alarm alarm = TestAlarm.builder()
                            .head(ALARM_HEAD + i)
                            .body(ALARM_BODY + i)
                            .user(user)
                            .build();
                    TestTimeReflection.setCreatedAt(alarm, LocalDateTime.now().minusDays(i));
                    return alarm;
                }).collect(Collectors.toList());
        alarmRepository.saveAll(alarms);

        persistenceUtil.cleanPersistenceContext();

        //when
        List<Alarm> findAlarms = alarmRepository.getAlarms(user.getId());

        List<Long> expectedAlarmIds = alarms.stream().map(Alarm::getId).collect(Collectors.toList());
        assertThat(findAlarms).extracting("id").isEqualTo(expectedAlarmIds);
        assertThat(findAlarms).extracting("head").containsExactly("헤드0", "헤드1", "헤드2");
        assertThat(findAlarms).extracting("body").containsExactly("바디0", "바디1", "바디2");
    }
}