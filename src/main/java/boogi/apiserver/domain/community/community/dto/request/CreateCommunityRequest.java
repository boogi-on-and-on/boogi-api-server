package boogi.apiserver.domain.community.community.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@NoArgsConstructor
@Getter
public class CreateCommunityRequest {

    @NotEmpty
    private String name;

    @NotEmpty
    private String category;

    @NotEmpty
    private String description;

    private List<String> hashtags;

    @NotNull
    private Boolean isPrivate;

    @NotNull
    private Boolean autoApproval;

    public CreateCommunityRequest(final String name, final String category, final String description, final List<String> hashtags, final Boolean isPrivate, final Boolean autoApproval) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.hashtags = hashtags;
        this.isPrivate = isPrivate;
        this.autoApproval = autoApproval;
    }
}
