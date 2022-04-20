package boogi.apiserver.domain.comment.domain;

import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.post.post.domain.Post;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "COMMENT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Comment extends TimeBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @JoinColumn(name = "post_id")
    @ManyToOne(fetch = LAZY)
    private Post post;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = LAZY)
    private Member member;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    private String content;

    private Boolean child;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private Comment(Post post, Member member, Comment parent, String content) {
        this.post = post;
        this.member = member;
        this.parent = parent;
        this.content = content;
        this.child = null;
    }

    private Comment(Long id, String content) {
        this.id = id;
        this.content = content;
    }

    public static Comment of(Post post, Member member, Comment parent, String content) {
        return new Comment(post, member, parent, content);
    }

    public static Comment deletedOf(Long id) {
        return new Comment(id, "삭제된 댓글입니다");
    }

    public void setChild(Boolean isChild) {
        this.child = isChild;
    }

    public void deleteComment() {
        this.deletedAt = LocalDateTime.now();
    }
}
