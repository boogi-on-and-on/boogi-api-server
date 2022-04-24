package boogi.apiserver.domain.alarm.alarm.dao;

import boogi.apiserver.domain.alarm.alarm.domain.Alarm;

import java.util.List;

public interface AlarmRepositoryCustom {

    List<Alarm> getAlarms(Long userId);
}
