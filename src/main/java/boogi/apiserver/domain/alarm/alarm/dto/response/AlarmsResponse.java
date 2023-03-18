package boogi.apiserver.domain.alarm.alarm.dto.response;

import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.alarm.alarm.dto.dto.AlarmsDto;
import lombok.Getter;

import java.util.List;

@Getter
public class AlarmsResponse {
    List<AlarmsDto> alarms;

    public AlarmsResponse(List<AlarmsDto> alarms) {
        this.alarms = alarms;
    }

    public static AlarmsResponse from(List<Alarm> alarms) {
        List<AlarmsDto> alarmsDtos = AlarmsDto.listOf(alarms);

        return new AlarmsResponse(alarmsDtos);
    }
}
