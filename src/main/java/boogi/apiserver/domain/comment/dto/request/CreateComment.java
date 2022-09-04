package boogi.apiserver.domain.comment.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;


@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class CreateComment {

    @NotNull
    private Long postId;

    private Long parentCommentId;

    @Size(max = 250, message = "255자 이내로 입력해주세요")
    private String content;

    private List<Long> mentionedUserIds = new ArrayList<>();
}