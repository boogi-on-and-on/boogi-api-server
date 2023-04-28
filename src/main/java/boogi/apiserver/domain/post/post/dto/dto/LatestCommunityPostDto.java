package boogi.apiserver.domain.post.post.dto.dto;

import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LatestCommunityPostDto {
    private Long id;
    private String content;

    @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
    private LocalDateTime createdAt;

    @Builder
    public LatestCommunityPostDto(Long id, String content, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static LatestCommunityPostDto of(Post post) {
        return new LatestCommunityPostDto(post.getId(), post.getContent(), post.getCreatedAt());
    }

    public static List<LatestCommunityPostDto> listOf(List<Post> posts) {
        return posts.stream()
                .map(LatestCommunityPostDto::of)
                .collect(Collectors.toList());
    }
}
