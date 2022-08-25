package boogi.apiserver.domain.post.post.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;


@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class CreatePost {

    @NotNull(message = "글을 작성할 커뮤니티를 선택해주세요")
    private Long communityId;

    @NotEmpty(message = "내용을 입력해주세요")
    @Size(min = 1, max = 1000, message = "1000자 이내로 입력해주세요")
    private String content;

    private List<String> hashtags;

    private List<String> postMediaIds;

    private List<Long> mentionedUserIds = new ArrayList<>();
}
