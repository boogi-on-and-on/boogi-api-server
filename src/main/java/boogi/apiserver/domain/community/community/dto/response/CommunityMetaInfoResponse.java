package boogi.apiserver.domain.community.community.dto.response;

import boogi.apiserver.domain.community.community.dto.dto.CommunityMetadataDto;
import lombok.Getter;

@Getter
public class CommunityMetaInfoResponse {

    private final CommunityMetadataDto metadata;

    public CommunityMetaInfoResponse(CommunityMetadataDto metadata) {
        this.metadata = metadata;
    }

    public static CommunityMetaInfoResponse from(CommunityMetadataDto metadata) {
        return new CommunityMetaInfoResponse(metadata);
    }
}
