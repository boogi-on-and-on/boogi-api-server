package boogi.apiserver.domain.comment.domain;

import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.post.post.domain.Post;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Where(clause = "deleted_at is null")
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

    @Embedded
    private CommentContent content;

    private boolean child;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private Comment(Long id, Post post, Member member, Comment parent, String content,
                    boolean child, LocalDateTime deletedAt) {
        this.id = id;
        this.post = post;
        this.member = member;
        this.parent = parent;
        this.content = new CommentContent(content);
        this.child = child;
        this.deletedAt = deletedAt;
    }

    private Comment(Post post, Member member, Comment parent, String content) {
        this.post = post;
        this.member = member;
        this.parent = parent;
        this.content = new CommentContent(content);
        this.child = parent != null;
    }

    protected Comment(Long id, String content, LocalDateTime removeAt) {
        this.id = id;
        this.content = new CommentContent(content);
        this.deletedAt = removeAt;
    }

    public static Comment of(Post post, Member member, Comment parent, String content) {
        post.addCommentCount();
        return new Comment(post, member, parent, content);
    }

    public void deleteComment() {
        this.deletedAt = LocalDateTime.now();
        if (post != null) {
            post.removeCommentCount();
        }
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public Member getMember() {
        return member;
    }

    public Comment getParent() {
        return parent;
    }

    public String getContent() {
        return content.getValue();
    }

    public boolean getChild() {
        return child;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
