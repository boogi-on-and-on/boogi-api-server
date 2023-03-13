package boogi.apiserver.domain.alarm.alarm.dto.response;

import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.alarm.alarm.dto.dto.AlarmsDto;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class AlarmsResponse {
    List<AlarmsDto> alarms;

    private AlarmsResponse(List<AlarmsDto> alarms) {
        this.alarms = alarms;
    }

    public static AlarmsResponse from(List<Alarm> alarms) {
        List<AlarmsDto> alarmsDtos = alarms.stream()
                .map(AlarmsDto::of)
                .collect(Collectors.toList());

        return new AlarmsResponse(alarmsDtos);
    }
}
