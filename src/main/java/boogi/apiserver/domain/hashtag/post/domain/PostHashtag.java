package boogi.apiserver.domain.hashtag.post.domain;

import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.post.post.domain.Post;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "POST_HASHTAG")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PostHashtag extends TimeBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_hashtag_id")
    private Long id;

    @JoinColumn(name = "post_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    private String tag;

    @Builder
    private PostHashtag(final Long id, final Post post, final String tag) {
        this.id = id;
        this.post = post;
        this.tag = tag;
    }

    private PostHashtag(Post post, String tag) {
        this.post = post;
        this.tag = tag;
//        post.getHashtags().add(this); //todo
    }

    public static PostHashtag of(String tag, Post post) {
        return new PostHashtag(post, tag);
//        post.getHashtags().add(this); //todo??
    }
}
