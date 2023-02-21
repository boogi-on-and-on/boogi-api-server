package boogi.apiserver.domain.community.community.dto.response;

import boogi.apiserver.domain.community.community.dto.dto.CommunitySettingInfoDto;
import lombok.Getter;

@Getter
public class UpdateCommunityResponse {

    private final CommunitySettingInfoDto settingInfo;

    public UpdateCommunityResponse(CommunitySettingInfoDto settingInfo) {
        this.settingInfo = settingInfo;
    }

    public static UpdateCommunityResponse from(CommunitySettingInfoDto settingInfo) {
        return new UpdateCommunityResponse(settingInfo);
    }
}
