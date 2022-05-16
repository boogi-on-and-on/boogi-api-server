package boogi.apiserver.domain.report.domain;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "REPORT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class Report extends TimeBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @JoinColumn(name = "post_id")
    @ManyToOne(fetch = LAZY)
    private Post post;

    @JoinColumn(name = "community_id")
    @ManyToOne(fetch = LAZY)
    private Community community;

    @JoinColumn(name = "comment_id")
    @ManyToOne(fetch = LAZY)
    private Comment comment;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = LAZY)
    private User user;

    private String content;

    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    private Report(Post post, Community community, Comment comment, User user, String content, ReportReason reason) {
        this.post = post;
        this.community = community;
        this.comment = comment;
        this.user = user;
        this.content = content;
        this.reason = reason;
    }

    public static Report of(Object targetObject, User user, String content, ReportReason reason) {
        if (targetObject instanceof Community) {
            return new Report(null, (Community) targetObject, null, user, content, reason);
        } else if (targetObject instanceof Post) {
            return new Report((Post) targetObject, null, null, user, content, reason);
        } else if (targetObject instanceof Comment) {
            return new Report(null, null, (Comment) targetObject, user, content, reason);
        } else {
            throw new InvalidValueException("잘못된 신고 대상입니다");
        }
    }
}
