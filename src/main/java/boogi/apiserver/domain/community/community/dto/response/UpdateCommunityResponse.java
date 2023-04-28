package boogi.apiserver.domain.community.community.dto.response;

import boogi.apiserver.domain.community.community.dto.dto.CommunitySettingInfoDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateCommunityResponse {

    private CommunitySettingInfoDto settingInfo;

    public UpdateCommunityResponse(CommunitySettingInfoDto settingInfo) {
        this.settingInfo = settingInfo;
    }

    public static UpdateCommunityResponse from(CommunitySettingInfoDto settingInfo) {
        return new UpdateCommunityResponse(settingInfo);
    }
}
