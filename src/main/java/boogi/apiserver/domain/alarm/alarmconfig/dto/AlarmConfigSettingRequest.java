package boogi.apiserver.domain.alarm.alarmconfig.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmConfigSettingRequest {

    private Boolean message;
    private Boolean notice;
    private Boolean join;
    private Boolean comment;
    private Boolean mention;
}
