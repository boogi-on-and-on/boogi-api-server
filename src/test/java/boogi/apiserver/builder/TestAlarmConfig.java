package boogi.apiserver.builder;

import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;

public class TestAlarmConfig {
    public static AlarmConfig.AlarmConfigBuilder builder() {
        return AlarmConfig.builder()
                .message(true)
                .notice(true)
                .joinRequest(true)
                .comment(true)
                .mention(true);
    }
}
