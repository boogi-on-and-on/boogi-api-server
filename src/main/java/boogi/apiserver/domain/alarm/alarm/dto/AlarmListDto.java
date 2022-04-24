package boogi.apiserver.domain.alarm.alarm.dto;

import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlarmListDto {
    private Long id;
    private String head;
    private String body;
    private String createdAt;

    private AlarmListDto(Alarm alarm) {
        this.id = alarm.getId();
        this.head = alarm.getHead();
        this.body = alarm.getBody();
        this.createdAt = alarm.getCreatedAt().toString();
    }

    public static AlarmListDto of(Alarm alarm) {
        return new AlarmListDto(alarm);
    }

}
