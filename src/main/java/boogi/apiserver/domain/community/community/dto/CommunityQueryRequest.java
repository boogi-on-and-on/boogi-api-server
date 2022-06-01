package boogi.apiserver.domain.community.community.dto;

import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.dto.request_enum.CommunityListingOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommunityQueryRequest {
    private CommunityCategory category;

    private Boolean isPrivate;

    @NotNull
    private CommunityListingOrder order = CommunityListingOrder.NEWER;

    private String keyword;
}
