package boogi.apiserver.domain.post.post.dto;

import boogi.apiserver.domain.post.post.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class HotPost {
    private String id;
    private String likeCount;
    private String commentCount;
    private String content;

    private HotPost(Post post) {
        this.id = post.getId().toString();
        this.likeCount = post.getLikeCount().toString();
        this.commentCount = post.getCommentCount().toString();
        this.content = post.getContent();
    }

    public static HotPost of(Post post) {
        return new HotPost(post);
    }
}
