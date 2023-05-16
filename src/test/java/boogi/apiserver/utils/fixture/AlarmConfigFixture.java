package boogi.apiserver.utils.fixture;

import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.utils.TestTimeReflection;

import java.time.LocalDateTime;

import static boogi.apiserver.utils.fixture.TimeFixture.STANDARD;

public enum AlarmConfigFixture {
    ALL_TRUE(true, true, true, true, true, STANDARD),
    ALL_FALSE(false, false, false, false, false, STANDARD.plusDays(1)),
    ;

    public final boolean message;
    public final boolean notice;
    public final boolean joinRequest;
    public final boolean comment;
    public final boolean mention;
    public final LocalDateTime createdAt;

    AlarmConfigFixture(boolean message, boolean notice, boolean joinRequest, boolean comment, boolean mention, LocalDateTime createdAt) {
        this.message = message;
        this.notice = notice;
        this.joinRequest = joinRequest;
        this.comment = comment;
        this.mention = mention;
        this.createdAt = createdAt;
    }

    public AlarmConfig toAlarmConfig(Long id) {
        AlarmConfig alarmConfig = AlarmConfig.builder()
                .id(id)
                .message(this.message)
                .notice(this.notice)
                .joinRequest(this.joinRequest)
                .comment(this.comment)
                .mention(this.mention)
                .build();
        TestTimeReflection.setCreatedAt(alarmConfig, this.createdAt);
        return alarmConfig;
    }

    public AlarmConfig toAlarmConfig() {
        return toAlarmConfig(null);
    }
}
