package boogi.apiserver.domain.community.community.dto.request;

import lombok.*;

@NoArgsConstructor
@Getter
public class CommunitySettingRequest {

    private Boolean isSecret;
    private Boolean isAutoApproval;

    public CommunitySettingRequest(Boolean isSecret, Boolean isAutoApproval) {
        this.isSecret = isSecret;
        this.isAutoApproval = isAutoApproval;
    }
}
