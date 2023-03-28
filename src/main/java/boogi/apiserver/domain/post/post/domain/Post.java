package boogi.apiserver.domain.post.post.domain;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtags;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.domain.PostMedias;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
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
@Where(clause = "deleted_at is null")
@SQLDelete(sql = "UPDATE post SET deleted_at = now(), like_count = 0 WHERE post_id = ?")
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

    @Embedded
    private PostContent content;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "like_count")
    private int likeCount;

    @Column(name = "comment_count")
    private int commentCount;

    @Embedded
    private PostHashtags hashtags = new PostHashtags();

    @Embedded
    private PostMedias postMedias = new PostMedias();

    @OneToMany(mappedBy = "post")
    private List<Like> likes = new ArrayList<>();

    @Builder
    private Post(Long id, Community community, Member member, String content, LocalDateTime deletedAt, int likeCount,
                 int commentCount, List<PostHashtag> hashtags, List<PostMedia> postMedias, List<Like> likes) {
        this.id = id;
        this.community = community;
        this.member = member;
        this.content = new PostContent(content);
        this.deletedAt = deletedAt;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.hashtags = new PostHashtags(hashtags);
        this.postMedias = new PostMedias(postMedias);
        this.likes = likes;
    }

    private Post(Community community, Member member, String content) {
        this.community = community;
        this.member = member;
        this.content = new PostContent(content);
    }

    public static Post of(Community community, Member member, String content) {
        return new Post(community, member, content);
    }

    public void addTags(List<String> tags) {
        this.hashtags.addTags(tags, this);
    }

    public void addPostMedias(List<PostMedia> postMedia) {
        this.postMedias.addPostMedias(postMedia, this);
    }

    public void updatePost(String content, List<String> tags, List<PostMedia> postMedias) {
        this.content = new PostContent(content);
        this.hashtags.updateTags(tags, this);
        this.postMedias.updatePostMedias(postMedias, this);
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

    public boolean isAuthor(Long userId) {
        Long postUserId = getMember().getUser().getId();
        return postUserId.equals(userId);
    }

    public Long getCommunityId() {
        return getCommunity().getId();
    }

    public Long getId() {
        return id;
    }

    public Community getCommunity() {
        return community;
    }

    public Member getMember() {
        return member;
    }

    public String getContent() {
        return content.getValue();
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public List<PostHashtag> getHashtags() {
        return hashtags.getValues();
    }

    public List<PostMedia> getPostMedias() {
        return postMedias.getValues();
    }

    public List<Like> getLikes() {
        return likes;
    }
}
