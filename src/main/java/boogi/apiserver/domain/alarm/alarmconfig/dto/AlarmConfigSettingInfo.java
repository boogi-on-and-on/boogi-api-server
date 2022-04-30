package boogi.apiserver.domain.alarm.alarmconfig.dto;

import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
public class AlarmConfigSettingInfo {

    private Personal personal;
    private Community community;
    private Post post;

    private AlarmConfigSettingInfo(AlarmConfig alarmConfig) {
        this.personal = Personal.builder()
                .message(Objects.requireNonNullElse(alarmConfig.getMessage(), false))
                .build();

        this.community = Community.builder()
                .join(Objects.requireNonNullElse(alarmConfig.getJoinRequest(), false))
                .notice(Objects.requireNonNullElse(alarmConfig.getNotice(), false))
                .build();

        this.post = Post.builder()
                .comment(Objects.requireNonNullElse(alarmConfig.getComment(), false))
                .mention(Objects.requireNonNullElse(alarmConfig.getMention(), false))
                .build();

    }

    public static AlarmConfigSettingInfo of(AlarmConfig alarmConfig) {
        return new AlarmConfigSettingInfo(alarmConfig);
    }

    @Data
    @Builder
    public static class Personal {
        private Boolean message;
    }

    @Data
    @Builder
    public static class Community {
        private Boolean notice;
        private Boolean join;
    }

    @Data
    @Builder
    public static class Post {
        private Boolean comment;
        private Boolean mention;
    }
}
