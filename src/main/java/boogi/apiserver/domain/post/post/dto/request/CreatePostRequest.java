package boogi.apiserver.domain.post.post.dto.request;


import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;


@Getter
@NoArgsConstructor
public class CreatePostRequest {

    @NotNull(message = "글을 작성할 커뮤니티를 선택해주세요")
    private Long communityId;

    @NotEmpty(message = "내용을 입력해주세요")
    @Size(min = 1, max = 1000, message = "1000자 이내로 입력해주세요")
    private String content;

    private List<String> hashtags;

    @NotNull
    private List<String> postMediaIds;

    private List<Long> mentionedUserIds = new ArrayList<>();

    public CreatePostRequest(Long communityId, String content, List<String> hashtags, List<String> postMediaIds, List<Long> mentionedUserIds) {
        this.communityId = communityId;
        this.content = content;
        this.hashtags = hashtags;
        this.postMediaIds = postMediaIds;
        this.mentionedUserIds = mentionedUserIds;
    }
}
