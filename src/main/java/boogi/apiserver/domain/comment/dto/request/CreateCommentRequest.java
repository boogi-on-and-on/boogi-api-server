package boogi.apiserver.domain.comment.dto.request;


import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


@Getter
@NoArgsConstructor
public class CreateCommentRequest {

    @NotNull(message = "댓글이 작성될 글을 선택해주세요")
    private Long postId;

    private Long parentCommentId;

    @NotBlank(message = "댓글의 내용을 입력해주세요")
    private String content;

    private List<Long> mentionedUserIds = new ArrayList<>();

    public CreateCommentRequest(Long postId, Long parentCommentId, String content, List<Long> mentionedUserIds) {
        this.postId = postId;
        this.parentCommentId = parentCommentId;
        this.content = content;
        this.mentionedUserIds = mentionedUserIds;
    }
}