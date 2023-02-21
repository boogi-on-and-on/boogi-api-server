package boogi.apiserver.domain.community.community.dto.response;

import boogi.apiserver.domain.community.community.dto.dto.SearchCommunityDto;
import boogi.apiserver.global.dto.PaginationDto;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
public class CommunityQueryResponse {
    private final PaginationDto pageInfo;
    private final List<SearchCommunityDto> communities;

    public CommunityQueryResponse(final PaginationDto pageInfo, final List<SearchCommunityDto> communities) {
        this.pageInfo = pageInfo;
        this.communities = communities;
    }

    public static CommunityQueryResponse from(final Slice<SearchCommunityDto> slice) {
        return new CommunityQueryResponse(PaginationDto.of(slice), slice.getContent());
    }
}
