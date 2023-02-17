package boogi.apiserver.domain.community.community.dto.response;

import boogi.apiserver.domain.community.community.dto.dto.CommunityMetadataDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CommunityMetaInfoResponse {

    final CommunityMetadataDto metadata;

    @Builder(access = AccessLevel.PRIVATE)
    private CommunityMetaInfoResponse(final CommunityMetadataDto metadata) {
        this.metadata = metadata;
    }

    public static CommunityMetaInfoResponse from(CommunityMetadataDto metadata) {
        return CommunityMetaInfoResponse.builder()
                .metadata(metadata)
                .build();
    }
}
