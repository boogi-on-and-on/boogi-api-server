package boogi.apiserver.domain.alarm.alarm.repository;

import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.alarm.alarm.exception.AlarmNotFoundException;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.utils.RepositoryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static boogi.apiserver.utils.fixture.AlarmFixture.*;
import static boogi.apiserver.utils.fixture.UserFixture.SUNDO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

class AlarmRepositoryTest extends RepositoryTest {

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private UserRepository userRepository;

    private Alarm alarm1;
    private Alarm alarm2;
    private Alarm alarm3;
    private User user;

    @BeforeEach
    public void init() {
        this.user = SUNDO.toUser();
        this.alarm1 = ALARM1.toAlarm(this.user);
        this.alarm2 = ALARM2.toAlarm(this.user);
        this.alarm3 = ALARM3.toAlarm(this.user);
    }

    @Nested
    @DisplayName("알람 ID로 알람 조회")
    class findAlarmById {

        @DisplayName("성공")
        @Test
        void success() {
            //given
            userRepository.save(user);
            alarmRepository.save(alarm1);

            //when
            final Alarm findAlarm = alarmRepository.findAlarmById(alarm1.getId());

            //then
            assertThat(findAlarm.getId()).isEqualTo(alarm1.getId());
        }

        @DisplayName("throw AlarmNotFoundException")
        @Test
        void throwException() {
            //then
            assertThatThrownBy(() -> {
                //when
                alarmRepository.findAlarmById(1L);
            }).isInstanceOf(AlarmNotFoundException.class);
        }
    }

    @Test
    @DisplayName("유저 ID로 알람을 최신순으로 조회한다.")
    void getAlarms() {
        //given
        userRepository.save(user);
        alarmRepository.saveAll(List.of(alarm1, alarm2, alarm3));

        //when
        List<Alarm> findAlarms = alarmRepository.getAlarms(user.getId());

        //then
        assertThat(findAlarms)
                .extracting("id", "head", "body")
                .containsExactly(
                        tuple(alarm1.getId(), ALARM1.head, ALARM1.body),
                        tuple(alarm2.getId(), ALARM2.head, ALARM2.body),
                        tuple(alarm3.getId(), ALARM3.head, ALARM3.body)
                );
    }
}