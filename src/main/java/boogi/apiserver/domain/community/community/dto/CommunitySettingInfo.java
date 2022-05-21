package boogi.apiserver.domain.community.community.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import lombok.Data;

import java.util.Objects;

@Data
public class CommunitySettingInfo {
    private Boolean isAuto;
    private Boolean isSecrete;

    private CommunitySettingInfo(Community community) {
        this.isAuto = Objects.requireNonNullElse(community.isAutoApproval(), true);
        this.isSecrete = Objects.requireNonNullElse(community.isPrivate(), false);
    }

    public static CommunitySettingInfo of(Community community) {
        return new CommunitySettingInfo(community);
    }
}