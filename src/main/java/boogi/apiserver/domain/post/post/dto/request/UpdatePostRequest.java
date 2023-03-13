package boogi.apiserver.domain.post.post.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@NoArgsConstructor
public class UpdatePostRequest {

    @NotEmpty(message = "내용을 입력해주세요")
    @Size(min = 1, max = 1000, message = "1000자 이내로 입력해주세요")
    private String content;

    private List<String> hashtags;

    private List<String> postMediaIds;

    public UpdatePostRequest(String content, List<String> hashtags, List<String> postMediaIds) {
        this.content = content;
        this.hashtags = hashtags;
        this.postMediaIds = postMediaIds;
    }
}