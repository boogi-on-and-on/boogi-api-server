package boogi.apiserver.domain.alarm.alarmconfig.application;

import boogi.apiserver.domain.alarm.alarmconfig.dao.AlarmConfigRepository;
import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlarmConfigCoreService {

    private final AlarmConfigRepository alarmConfigRepository;
    private final UserQueryService userQueryService;

    @Transactional
    public AlarmConfig findOrCreateAlarm(Long userId) {
        AlarmConfig alarmConfig = alarmConfigRepository.getAlarmConfigByUserId(userId);

        if (alarmConfig == null) {
            User user = userQueryService.getUser(userId);
            AlarmConfig newAlarmConfig = AlarmConfig.of(user);
            return alarmConfigRepository.save(newAlarmConfig);
        }
        return alarmConfig;
    }
}
