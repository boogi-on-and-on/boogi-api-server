package boogi.apiserver.domain.user.dto.response;

import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.alarm.alarmconfig.dto.dto.AlarmConfigSettingInfoDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlarmConfigSettingInfoResponse {

    private AlarmConfigSettingInfoDto alarmInfo;

    public AlarmConfigSettingInfoResponse(AlarmConfigSettingInfoDto alarmInfo) {
        this.alarmInfo = alarmInfo;
    }

    public static AlarmConfigSettingInfoResponse from(AlarmConfig alarmConfig) {
        AlarmConfigSettingInfoDto alarmInfo = AlarmConfigSettingInfoDto.of(alarmConfig);
        return new AlarmConfigSettingInfoResponse(alarmInfo);
    }
}
