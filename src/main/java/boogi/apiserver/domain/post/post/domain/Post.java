package boogi.apiserver.domain.post.post.domain;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "POST")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Where(clause = "deleted_at is null")
@SQLDelete(sql = "UPDATE post SET deleted_at = now(), likeCount = 0 WHERE post_id = ?")
public class Post extends TimeBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @JoinColumn(name = "community_id")
    @ManyToOne(fetch = LAZY)
    private Community community;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = LAZY)
    private Member member;

    private String content;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "like_count")
    private Integer likeCount;

    @Column(name = "comment_count")
    private Integer commentCount;

    @OneToMany(mappedBy = "post")
    List<PostHashtag> hashtags = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    List<PostMedia> postMedias = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    List<Like> likes = new ArrayList<>();

    private Post(Community community, Member member, String content) {
        this.community = community;
        this.member = member;
        this.content = content;
        this.commentCount = 0;
        this.likeCount = 0;
    }

    public static Post of(Community community, Member member, String content) {
        return new Post(community, member, content);
    }

    public void addLikeCount() {
        this.likeCount++;
    }

    public void removeLikeCount() {
        if (this.likeCount > 0)
            this.likeCount--;
    }

    public void addCommentCount() {
        this.commentCount++;
    }

    public void removeCommentCount() {
        if (this.commentCount > 0)
            this.commentCount--;
    }

    public void updatePost(String content, List<PostHashtag> hashtags) {
        this.content = content;
        this.hashtags = hashtags;
    }
}
