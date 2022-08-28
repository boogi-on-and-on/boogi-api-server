package boogi.apiserver.domain.post.post.dto.response;

import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class LatestPostOfCommunityDto {
    private Long id;
    private String content;

    @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
    private LocalDateTime createdAt;

    private LatestPostOfCommunityDto(Post post) {
        this.id = post.getId();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
    }

    public static LatestPostOfCommunityDto of(Post post) {
        return new LatestPostOfCommunityDto(post);
    }
}
