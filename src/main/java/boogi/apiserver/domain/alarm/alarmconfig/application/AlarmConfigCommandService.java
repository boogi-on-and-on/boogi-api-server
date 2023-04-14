package boogi.apiserver.domain.alarm.alarmconfig.application;

import boogi.apiserver.domain.alarm.alarmconfig.repository.AlarmConfigRepository;
import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.alarm.alarmconfig.dto.request.AlarmConfigSettingRequest;
import boogi.apiserver.domain.user.repository.UserRepository;
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
        return alarmConfigRepository.getAlarmConfigByUserId(userId)
                .orElseGet(() -> createAlarmConfig(userId));
    }

    public AlarmConfig configureAlarm(Long userId, AlarmConfigSettingRequest request) {
        AlarmConfig alarmConfig = this.findOrElseCreateAlarmConfig(userId);

        alarmConfig.switchMessage(request.getMessage());
        alarmConfig.switchNotice(request.getNotice());
        alarmConfig.switchJoinRequest(request.getJoin());
        alarmConfig.switchComment(request.getComment());
        alarmConfig.switchMention(request.getMention());

        return alarmConfig;
    }

    private AlarmConfig createAlarmConfig(Long userId) {
        User user = userRepository.findUserById(userId);
        return alarmConfigRepository.save(AlarmConfig.of(user));
    }
}
