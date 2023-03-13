package boogi.apiserver.domain.hashtag.post.domain;

import boogi.apiserver.domain.hashtag.domain.Tag;
import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.post.post.domain.Post;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "POST_HASHTAG")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostHashtag extends TimeBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_hashtag_id")
    private Long id;

    @JoinColumn(name = "post_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    private Tag tag;

    @Builder
    private PostHashtag(final Long id, final Post post, final String tag) {
        this.id = id;
        this.post = post;
        this.tag = new Tag(tag);
    }

    private PostHashtag(Post post, String tag) {
        this.post = post;
        this.tag = new Tag(tag);
    }

    public static PostHashtag of(String tag, Post post) {
        return new PostHashtag(post, tag);
    }

    // 해당 코드는 PostHashtags의 메서드를 통해 사용하지 않으면 OneToMany 연관관계상 문제가 생길 수 있습니다.
    public static List<PostHashtag> listOf(List<String> tags, Post post) {
        return tags.stream()
                .map(tag -> PostHashtag.of(tag, post))
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public String getTag() {
        return tag.getValue();
    }
}
