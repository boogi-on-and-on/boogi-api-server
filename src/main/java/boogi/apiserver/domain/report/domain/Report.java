package boogi.apiserver.domain.report.domain;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @JoinColumn(name = "message_id")
    @ManyToOne(fetch = LAZY)
    private Message message;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = LAZY)
    private User user;

    @Embedded
    private ReportContent content;

    @Enumerated(EnumType.STRING)
    private ReportReason reason;


    @Builder
    private Report(Long id, Post post, Community community, Comment comment, Message message, User user, String content,
                   ReportReason reason) {
        this.id = id;
        this.post = post;
        this.community = community;
        this.comment = comment;
        this.message = message;
        this.user = user;
        this.content = new ReportContent(content);
        this.reason = reason;
    }

    private Report(Post post, User user, String content, ReportReason reason) {
        this(null, post, null, null, null, user, content, reason);
    }

    private Report(Community community, User user, String content, ReportReason reason) {
        this(null, null, community, null, null, user, content, reason);
    }

    private Report(Comment comment, User user, String content, ReportReason reason) {
        this(null, null, null, comment, null, user, content, reason);
    }

    private Report(Message message, User user, String content, ReportReason reason) {
        this(null, null, null, null, message, user, content, reason);
    }

    public static Report of(Object targetObject, User user, String content, ReportReason reason) {
        if (targetObject instanceof Community) {
            return new Report((Community) targetObject, user, content, reason);
        } else if (targetObject instanceof Post) {
            return new Report((Post) targetObject, user, content, reason);
        } else if (targetObject instanceof Comment) {
            return new Report((Comment) targetObject, user, content, reason);
        } else if (targetObject instanceof Message) {
            return new Report((Message) targetObject, user, content, reason);
        }
        throw new IllegalArgumentException();
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public Community getCommunity() {
        return community;
    }

    public Comment getComment() {
        return comment;
    }

    public Message getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public String getContent() {
        return content.getValue();
    }

    public ReportReason getReason() {
        return reason;
    }
}
