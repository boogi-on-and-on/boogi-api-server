package boogi.apiserver.domain.alarm.alarmconfig.dao;

import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;

public interface AlarmConfigRepositoryCustom {
    AlarmConfig getAlarmConfigByUserId(Long userId);
}
