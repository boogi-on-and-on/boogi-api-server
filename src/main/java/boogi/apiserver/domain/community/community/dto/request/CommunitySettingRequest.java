package boogi.apiserver.domain.community.community.dto.request;

import lombok.*;

@NoArgsConstructor
@Getter
public class CommunitySettingRequest {

    private Boolean isSecret;
    private Boolean isAutoApproval;

    public CommunitySettingRequest(final Boolean isSecret, final Boolean isAutoApproval) {
        this.isSecret = isSecret;
        this.isAutoApproval = isAutoApproval;
    }
}
