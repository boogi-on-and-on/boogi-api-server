package boogi.apiserver.domain.community.community.dto.request;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@Getter
public class UpdateCommunityRequest {

    @NotEmpty(message = "커뮤니티 소개란을 입력해주세요.")
    @Size(min = 10, message = "10글자 이상 소개란을 입력해주세요.")
    private String description;

    @Size(max = 5, message = "해시테그는 5개까지만 입력가능합니다.")
    private List<String> hashtags;

    public UpdateCommunityRequest(final String description, final List<String> hashtags) {
        this.description = description;
        this.hashtags = hashtags;
    }
}
