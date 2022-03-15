package boogi.apiserver.domain.community.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommunityRequest {

    @NotEmpty
    private String name;

    @NotEmpty
    private String category;

    @NotEmpty
    private String description;

    private List<String> hashtags;

//    @NotEmpty
    private boolean isPrivate;

//    @NotEmpty
    private boolean autoApproval;
}
