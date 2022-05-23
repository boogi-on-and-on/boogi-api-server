package boogi.apiserver.domain.comment.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.Size;
import java.util.List;


@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class CreateComment {

    private Long postId;

    private Long parentCommentId;

    @Size(max = 250, message = "255자 이내로 입력해주세요")
    private String content;

    private List<Long> mentionedUserIds;
}