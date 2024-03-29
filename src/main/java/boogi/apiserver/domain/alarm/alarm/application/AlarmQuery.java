package boogi.apiserver.domain.alarm.alarm.application;

import boogi.apiserver.domain.alarm.alarm.repository.AlarmRepository;
import boogi.apiserver.domain.alarm.alarm.dto.response.AlarmsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AlarmQuery {

    private final AlarmRepository alarmRepository;

    public AlarmsResponse getAlarms(Long userId) {
        return AlarmsResponse.from(alarmRepository.getAlarms(userId));
    }
}
