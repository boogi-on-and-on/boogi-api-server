package boogi.apiserver.domain.alarm.alarmconfig.application;

import boogi.apiserver.domain.alarm.alarmconfig.dao.AlarmConfigRepository;
import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.alarm.alarmconfig.dto.AlarmConfigSettingRequest;
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
    public AlarmConfig findOrElseCreateAlarmConfig(Long userId) {
        AlarmConfig alarmConfig = alarmConfigRepository.getAlarmConfigByUserId(userId);

        if (alarmConfig == null) {
            User user = userQueryService.getUser(userId);
            AlarmConfig newAlarmConfig = AlarmConfig.of(user);
            return alarmConfigRepository.save(newAlarmConfig);
        }
        return alarmConfig;
    }

    @Transactional
    public AlarmConfig configureAlarm(Long userId, AlarmConfigSettingRequest config) {
        AlarmConfig alarmConfig = this.findOrElseCreateAlarmConfig(userId);

        Boolean message = config.getMessage();
        if (message != null) {
            alarmConfig.setMessage(message);
        }

        Boolean notice = config.getNotice();
        if (notice != null) {
            alarmConfig.setNotice(notice);
        }

        Boolean joinRequest = config.getJoin();
        if (joinRequest != null) {
            alarmConfig.setJoinRequest(joinRequest);
        }

        Boolean comment = config.getComment();
        if (comment != null) {
            alarmConfig.setComment(comment);
        }

        Boolean mention = config.getMention();
        if (mention != null) {
            alarmConfig.setMention(mention);
        }

        return alarmConfig;
    }
}
