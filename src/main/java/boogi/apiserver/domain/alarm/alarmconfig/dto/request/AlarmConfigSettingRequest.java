package boogi.apiserver.domain.alarm.alarmconfig.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AlarmConfigSettingRequest {

    private Boolean message;
    private Boolean notice;
    private Boolean join;
    private Boolean comment;
    private Boolean mention;

    public AlarmConfigSettingRequest(Boolean message, Boolean notice, Boolean join, Boolean comment, Boolean mention) {
        this.message = message;
        this.notice = notice;
        this.join = join;
        this.comment = comment;
        this.mention = mention;
    }
}
