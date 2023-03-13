package boogi.apiserver.domain.alarm.alarm.domain;

import boogi.apiserver.builder.TestAlarm;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.user.domain.User;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


class AlarmTest {

    @Nested
    @DisplayName("파라미터의 userId와 알람의 userId 비교")
    class CheckAlarmUserId {
        @Test
        @DisplayName("user Id에 맞는 알람이면 성공")
        void isSameUser() {
            final User user = TestUser.builder()
                    .id(1L)
                    .build();

            final Alarm alarm = TestAlarm.builder()
                    .user(user)
                    .build();

            final boolean isSameUser = alarm.isSameUser(1L);

            assertThat(isSameUser).isTrue();
        }

        @Test
        @DisplayName("userId가 다르면 실패")
        void isNotSameUser() {

            final User user = TestUser.builder()
                    .id(1L)
                    .build();

            final Alarm alarm = TestAlarm.builder()
                    .user(user)
                    .build();

            final boolean notSame = alarm.isSameUser(2L);

            assertThat(notSame).isFalse();
        }
    }
}