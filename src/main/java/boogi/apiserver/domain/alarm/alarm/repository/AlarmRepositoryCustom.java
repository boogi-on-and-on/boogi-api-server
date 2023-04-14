package boogi.apiserver.domain.alarm.alarm.repository;

import boogi.apiserver.domain.alarm.alarm.domain.Alarm;

import java.util.List;

public interface AlarmRepositoryCustom {
    List<Alarm> getAlarms(Long userId);
}
