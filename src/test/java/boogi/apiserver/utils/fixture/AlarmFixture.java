package boogi.apiserver.utils.fixture;

import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.TestTimeReflection;

import java.time.LocalDateTime;

import static boogi.apiserver.utils.fixture.TimeFixture.STANDARD;

public enum AlarmFixture {
    ALARM1("이번주 모임 공지", "이번 모임은 공학관에서 진행합니다. 필참하시기 바랍니다.", STANDARD),
    ALARM2("신입 환영회 진행 예정 공지", "신입환영회를 진행합니다. 반갑습니다.", STANDARD.plusDays(1)),
    ;

    public final String head;
    public final String body;
    public final LocalDateTime createdAt;

    AlarmFixture(String head, String body, LocalDateTime createdAt) {
        this.head = head;
        this.body = body;
        this.createdAt = createdAt;
    }

    public Alarm toAlarm(Long id, User user) {
        Alarm alarm = Alarm.builder()
                .id(id)
                .user(user)
                .head(this.head)
                .body(this.body)
                .build();
        TestTimeReflection.setCreatedAt(alarm, this.createdAt);
        return alarm;
    }

    public Alarm toAlarm(User user) {
        return toAlarm(null, user);
    }
}
