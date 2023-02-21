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
@Table(name = "COMMENT")
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
    private Content content;

    private boolean child;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private static final String DELETED_COMMENT_CONTENT = "삭제된 댓글입니다";

    @Builder
    private Comment(final Long id, final Post post, final Member member, final Comment parent, final String content,
                    final boolean child, final LocalDateTime deletedAt) {
        this.id = id;
        this.post = post;
        this.member = member;
        this.parent = parent;
        this.content = new Content(content);
        this.child = child;
        this.deletedAt = deletedAt;
    }

    public static Comment of(Post post, Member member, Comment parent, String content) {
        return Comment.builder()
                .post(post)
                .member(member)
                .parent(parent)
                .content(content)
                .child((parent == null) ? Boolean.FALSE : Boolean.TRUE)
                .build();
    }

    public static Comment deletedOf(Long id, LocalDateTime removeAt) {
        return Comment
                .builder()
                .id(id)
                .content(DELETED_COMMENT_CONTENT)
                .deletedAt(removeAt)
                .build();
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
