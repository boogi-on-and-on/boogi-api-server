package boogi.apiserver.domain.alarm.alarm.application;

import boogi.apiserver.domain.alarm.alarm.dao.AlarmRepository;
import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.alarm.alarm.dto.AlarmListDto;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AlarmQueryService {

    private final AlarmRepository alarmRepository;

    public Alarm getAlarm(Long alarmId) {
        Alarm alarm = alarmRepository.findById(alarmId).orElseThrow(EntityNotFoundException::new);
        return alarm;
    }

    public List<AlarmListDto> getAlarms(Long userId) {
        return alarmRepository.getAlarms(userId).stream()
                .map(AlarmListDto::of)
                .collect(Collectors.toList());
    }
}
