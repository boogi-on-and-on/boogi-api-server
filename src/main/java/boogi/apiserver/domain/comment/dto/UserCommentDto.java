package boogi.apiserver.domain.comment.dto;

import boogi.apiserver.domain.comment.domain.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class UserCommentDto {

    private String content;
    private String createdAt;
    private Long postId;

    private UserCommentDto(Comment comment) {
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt().toString();
        this.postId = comment.getPost().getId();
    }

    public static UserCommentDto of(Comment comment) {
        return new UserCommentDto(comment);
    }
}
