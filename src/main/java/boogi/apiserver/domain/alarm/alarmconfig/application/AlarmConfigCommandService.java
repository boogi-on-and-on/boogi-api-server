package boogi.apiserver.domain.alarm.alarmconfig.application;

import boogi.apiserver.domain.alarm.alarmconfig.dao.AlarmConfigRepository;
import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.alarm.alarmconfig.dto.request.AlarmConfigSettingRequest;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AlarmConfigCommandService {

    private final AlarmConfigRepository alarmConfigRepository;
    private final UserRepository userRepository;

    public AlarmConfig findOrElseCreateAlarmConfig(Long userId) {
        AlarmConfig alarmConfig = alarmConfigRepository.getAlarmConfigByUserId(userId);

        return alarmConfig == null ? createAlarmConfig(userId) : alarmConfig;
    }

    public AlarmConfig configureAlarm(Long userId, AlarmConfigSettingRequest config) {
        AlarmConfig alarmConfig = this.findOrElseCreateAlarmConfig(userId);

        alarmConfig.switchMessage(config.getMessage());
        alarmConfig.switchNotice(config.getNotice());
        alarmConfig.switchJoinRequest(config.getJoin());
        alarmConfig.switchComment(config.getComment());
        alarmConfig.switchMention(config.getMention());

        return alarmConfig;
    }

    private AlarmConfig createAlarmConfig(final Long userId) {
        User user = userRepository.findByUserId(userId);
        return alarmConfigRepository.save(AlarmConfig.of(user));
    }
}
