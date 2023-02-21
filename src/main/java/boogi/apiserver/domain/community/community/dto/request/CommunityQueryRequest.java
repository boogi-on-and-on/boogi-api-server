package boogi.apiserver.domain.community.community.dto.request;

import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.dto.enums.CommunityListingOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class CommunityQueryRequest {
    private CommunityCategory category;

    private Boolean isPrivate;

    @NotNull
    private CommunityListingOrder order = CommunityListingOrder.NEWER;

    private String keyword;

    public CommunityQueryRequest(CommunityCategory category, Boolean isPrivate,
                                 CommunityListingOrder order, String keyword) {
        this.category = category;
        this.isPrivate = isPrivate;
        this.order = order;
        this.keyword = keyword;
    }
}
