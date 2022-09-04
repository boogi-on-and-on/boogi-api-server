package boogi.apiserver.domain.community.community.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommunitySettingRequest {

    private Boolean isSecret;
    private Boolean isAutoApproval;
}
