package boogi.apiserver.domain.post.post.dto.dto;

import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LatestCommunityPostDto {
    private Long id;
    private String content;

    @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
    private LocalDateTime createdAt;

    public LatestCommunityPostDto(final Long id, final String content, final LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static LatestCommunityPostDto of(Post post) {
        return new LatestCommunityPostDto(post.getId(), post.getContent(), post.getCreatedAt());
    }
}
