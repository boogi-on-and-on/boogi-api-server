package boogi.apiserver.domain.alarm.alarmconfig.dao;

import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;

import java.util.Optional;

public interface AlarmConfigRepositoryCustom {
    Optional<AlarmConfig> getAlarmConfigByUserId(Long userId);
}
