package boogi.apiserver.domain.user.dto.response;

import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.alarm.alarmconfig.dto.dto.AlarmConfigSettingInfoDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AlarmConfigSettingInfoResponse {

    private final AlarmConfigSettingInfoDto alarmInfo;

    @Builder(access = AccessLevel.PRIVATE)
    private AlarmConfigSettingInfoResponse(final AlarmConfigSettingInfoDto alarmInfo) {
        this.alarmInfo = alarmInfo;
    }

    public static AlarmConfigSettingInfoResponse from(final AlarmConfig alarmConfig) {
        return AlarmConfigSettingInfoResponse.builder()
                .alarmInfo(AlarmConfigSettingInfoDto.of(alarmConfig))
                .build();
    }
}
