package boogi.apiserver.domain.alarm.alarm.application;

import boogi.apiserver.domain.alarm.alarm.repository.AlarmRepository;
import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.alarm.alarm.exception.CanNotDeleteAlarmException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AlarmCommand {

    private final AlarmRepository alarmRepository;

    public void deleteAlarm(Long userId, Long alarmId) {
        Alarm alarm = alarmRepository.findAlarmById(alarmId);
        if (!alarm.isSameUser(userId)) {
            throw new CanNotDeleteAlarmException();
        }

        alarmRepository.delete(alarm);
    }
}
