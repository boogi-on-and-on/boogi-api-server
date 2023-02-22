package boogi.apiserver.domain.alarm.alarmconfig.domain;

import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.user.domain.User;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "ALARM_CONFIG")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AlarmConfig extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_config_id")
    private Long id;

    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name = "message_alarm")
    @Setter
    private boolean message = true;

    @Column(name = "notice_alarm")
    @Setter
    private boolean notice = true;

    @Column(name = "join_request_alarm")
    @Setter
    private boolean joinRequest = true;

    @Column(name = "comment_alarm")
    @Setter
    private boolean comment = true;

    @Column(name = "mention_alarm")
    @Setter
    private boolean mention = true;

    @Builder
    private AlarmConfig(final Long id, final User user, final boolean message, final boolean notice,
                        final boolean joinRequest, final boolean comment, final boolean mention) {
        this.id = id;
        this.user = user;
        this.message = message;
        this.notice = notice;
        this.joinRequest = joinRequest;
        this.comment = comment;
        this.mention = mention;
    }

    private AlarmConfig(User user) {
        this.user = user;
    }

    public static AlarmConfig of(User user) {
        return new AlarmConfig(user);
    }

    public boolean getMessage() {
        return message;
    }

    public boolean getNotice() {
        return notice;
    }

    public boolean getJoinRequest() {
        return joinRequest;
    }

    public boolean getComment() {
        return comment;
    }

    public boolean getMention() {
        return mention;
    }
}
