package boogi.apiserver.domain.post.postmedia.domain;

import boogi.apiserver.domain.post.post.domain.Post;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

@Entity
@Table(name = "POST_MEDIA")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PostMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_media_id")
    private Long id;

    @Column(name = "uuid")
    private String uuid;

    @JoinColumn(name = "post_id")
    @ManyToOne(fetch = LAZY)
    private Post post;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "media_type")
    private MediaType mediaType;

    @Column(name = "media_url")
    private String mediaURL;

    public void mapPost(Post post) {
        this.post = post;
    }
}
