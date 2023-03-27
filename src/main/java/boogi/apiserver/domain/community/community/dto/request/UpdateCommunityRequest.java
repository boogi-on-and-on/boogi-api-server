package boogi.apiserver.domain.community.community.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@NoArgsConstructor
@Getter
public class UpdateCommunityRequest {

    @NotEmpty(message = "커뮤니티 소개란을 입력해주세요.")
    private String description;

    private List<String> hashtags;

    public UpdateCommunityRequest(final String description, final List<String> hashtags) {
        this.description = description;
        this.hashtags = hashtags;
    }
}
