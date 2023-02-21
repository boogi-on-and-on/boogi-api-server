package boogi.apiserver.domain.comment.dto.request;


import lombok.Getter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;


@Getter
public class CreateCommentRequest {

    @NotNull(message = "댓글이 작성될 글을 선택해주세요")
    private Long postId;

    private Long parentCommentId;

    @Size(max = 255, message = "255자 이내로 입력해주세요")
    private String content;

    private List<Long> mentionedUserIds = new ArrayList<>();

    public CreateCommentRequest(final Long postId, final Long parentCommentId, final String content, final List<Long> mentionedUserIds) {
        this.postId = postId;
        this.parentCommentId = parentCommentId;
        this.content = content;
        this.mentionedUserIds = mentionedUserIds;
    }
}