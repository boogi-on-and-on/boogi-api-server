package boogi.apiserver.domain.comment.dto.response;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserCommentDto {

    private String content;
    @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
    private LocalDateTime createdAt;
    private Long postId;

    public UserCommentDto(final String content, final LocalDateTime createdAt, final Long postId) {
        this.content = content;
        this.createdAt = createdAt;
        this.postId = postId;
    }

    public static UserCommentDto of(Comment comment) {
        return new UserCommentDto(comment.getContent(), comment.getCreatedAt(), comment.getPost().getId());
    }
}
