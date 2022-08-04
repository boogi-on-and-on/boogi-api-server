package boogi.apiserver.domain.alarm.alarm.application;

import boogi.apiserver.domain.alarm.alarm.dao.AlarmRepository;
import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AlarmCoreService {

    private final AlarmQueryService alarmQueryService;

    private final AlarmRepository alarmRepository;

    @Transactional
    public void deleteAlarm(Long userId, Long alarmId) {
        Alarm alarm = alarmQueryService.getAlarm(alarmId);
        if (!Objects.equals(alarm.getUser().getId(), userId)) {
            throw new InvalidValueException("해당 알림을 삭제할 권한이 없습니다.");
        }

        alarmRepository.delete(alarm);
    }
}
