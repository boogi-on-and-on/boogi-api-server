package boogi.apiserver.domain.post.post.dto;

import boogi.apiserver.domain.post.post.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class LatestPostOfCommunityDto {
    private Long id;
    private String content;
    private String createdAt;

    private LatestPostOfCommunityDto(Post post) {
        this.id = post.getId();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt().toString();
    }

    public static LatestPostOfCommunityDto of(Post post) {
        return new LatestPostOfCommunityDto(post);
    }
}
