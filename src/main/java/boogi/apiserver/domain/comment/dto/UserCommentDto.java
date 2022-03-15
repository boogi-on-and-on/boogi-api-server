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
    private String at;
    private String postId;

    private UserCommentDto(Comment comment) {
        this.content = comment.getContent();
        this.at = comment.getCreatedAt().toString();
        this.postId = comment.getPost().getId().toString();
    }

    public static UserCommentDto of(Comment comment) {
        return new UserCommentDto(comment);
    }
}
