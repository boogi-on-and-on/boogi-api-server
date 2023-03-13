package boogi.apiserver.domain.community.community.dto.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import lombok.Getter;

import java.util.Objects;

@Getter
public class CommunitySettingInfoDto {
    private Boolean isAuto;
    private Boolean isSecret;

    public CommunitySettingInfoDto(Boolean isAuto, Boolean isSecret) {
        this.isAuto = isAuto;
        this.isSecret = isSecret;
    }

    public static CommunitySettingInfoDto of(Community community) {
        Boolean isAuto = Objects.requireNonNullElse(community.isAutoApproval(), true);
        Boolean isSecret = Objects.requireNonNullElse(community.isPrivate(), false);

        return new CommunitySettingInfoDto(isAuto, isSecret);
    }
}
