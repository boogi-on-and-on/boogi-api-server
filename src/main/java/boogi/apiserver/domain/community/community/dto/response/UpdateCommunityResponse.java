package boogi.apiserver.domain.community.community.dto.response;

import boogi.apiserver.domain.community.community.dto.dto.CommunitySettingInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UpdateCommunityResponse {

    final CommunitySettingInfo settingInfo;

    @Builder(access = AccessLevel.PRIVATE)
    private UpdateCommunityResponse(CommunitySettingInfo settingInfo) {
        this.settingInfo = settingInfo;
    }

    public static UpdateCommunityResponse from(CommunitySettingInfo settingInfo) {
        return UpdateCommunityResponse.builder()
                .settingInfo(settingInfo)
                .build();
    }
}
