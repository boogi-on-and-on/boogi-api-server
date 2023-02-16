package boogi.apiserver.domain.like.domain;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.post.post.domain.Post;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "LIKES")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Like extends TimeBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @JoinColumn(name = "post_id")
    @ManyToOne(fetch = LAZY)
    private Post post;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = LAZY)
    private Member member;

    @JoinColumn(name = "comment_id")
    @ManyToOne(fetch = LAZY)
    private Comment comment;

    private Like(Post post, Comment comment, Member member){
        this.post = post;
        this.member = member;
        this.comment = comment;
    }

    public static Like postOf(Post post, Member member){
        return new Like(post, null, member);
    }

    public static Like commentOf(Comment comment, Member member){
        return new Like(null, comment, member);
    }
}