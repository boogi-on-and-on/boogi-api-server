package boogi.apiserver.domain.alarm.alarmconfig.dto.dto;

import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import lombok.Getter;

import java.util.Objects;

@Getter
public class AlarmConfigSettingInfoDto {

    private Personal personal;
    private Community community;
    private Post post;

    public AlarmConfigSettingInfoDto(Personal personal, Community community, Post post) {
        this.personal = personal;
        this.community = community;
        this.post = post;
    }

    public static AlarmConfigSettingInfoDto of(AlarmConfig alarmConfig) {
        return new AlarmConfigSettingInfoDto(
                Personal.from(alarmConfig.getMessage()),
                Community.from(alarmConfig),
                Post.from(alarmConfig)
        );
    }

    @Getter
    public static class Personal {
        private Boolean message;

        public Personal(Boolean message) {
            this.message = message;
        }

        public static Personal from(Boolean message) {
            Boolean messageValue = Objects.requireNonNullElse(message, false);
            return new Personal(messageValue);
        }
    }

    @Getter
    public static class Community {
        private Boolean notice;
        private Boolean join;

        public Community(Boolean notice, Boolean join) {
            this.notice = notice;
            this.join = join;
        }

        public static Community from(AlarmConfig alarmConfig) {
            Boolean noticeValue = Objects.requireNonNullElse(alarmConfig.getNotice(), false);
            Boolean joinValue = Objects.requireNonNullElse(alarmConfig.getJoinRequest(), false);
            return new Community(noticeValue, joinValue);
        }
    }

    @Getter
    public static class Post {
        private Boolean comment;
        private Boolean mention;

        public Post(Boolean comment, Boolean mention) {
            this.comment = comment;
            this.mention = mention;
        }

        public static Post from(AlarmConfig alarmConfig) {
            Boolean commentValue = Objects.requireNonNullElse(alarmConfig.getComment(), false);
            Boolean mentionValue = Objects.requireNonNullElse(alarmConfig.getMention(), false);
            return new Post(commentValue, mentionValue);
        }
    }
}
