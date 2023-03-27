package boogi.apiserver.domain.community.community.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@NoArgsConstructor
@Getter
public class CreateCommunityRequest {

    @NotEmpty(message = "커뮤니티의 이름을 입력해주세요")
    private String name;

    @NotEmpty(message = "커뮤니티의 카테고리를 선택해주세요")
    private String category;

    @NotEmpty(message = "커뮤니티의 설명을 입력해주세요")
    private String description;

    private List<String> hashtags;

    @NotNull(message = "커뮤니티 공개 여부를 선택해주세요")
    private Boolean isPrivate;

    @NotNull(message = "커뮤니티 자동 가입 여부를 선택해주세요")
    private Boolean autoApproval;

    public CreateCommunityRequest(final String name, final String category, final String description,
                                  final List<String> hashtags, final Boolean isPrivate, final Boolean autoApproval) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.hashtags = hashtags;
        this.isPrivate = isPrivate;
        this.autoApproval = autoApproval;
    }
}
