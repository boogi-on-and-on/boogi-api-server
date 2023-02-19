package boogi.apiserver.domain.post.postmedia.domain;

import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.post.post.domain.Post;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "POST_MEDIA")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Where(clause = "deleted_at is null")
@SQLDelete(sql = "UPDATE post_media SET deleted_at = now() WHERE post_media_id = ?")
public class PostMedia extends TimeBaseEntity {

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

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void mapPost(Post post) {
        this.post = post;
//        post.getPostMedias().add(this); todo
    }
}
