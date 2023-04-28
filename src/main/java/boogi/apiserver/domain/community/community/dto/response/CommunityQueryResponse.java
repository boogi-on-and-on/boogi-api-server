package boogi.apiserver.domain.community.community.dto.response;

import boogi.apiserver.domain.community.community.dto.dto.SearchCommunityDto;
import boogi.apiserver.global.dto.PaginationDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityQueryResponse {
    private PaginationDto pageInfo;
    private List<SearchCommunityDto> communities;

    public CommunityQueryResponse(PaginationDto pageInfo, List<SearchCommunityDto> communities) {
        this.pageInfo = pageInfo;
        this.communities = communities;
    }

    public static CommunityQueryResponse from(Slice<SearchCommunityDto> slice) {
        return new CommunityQueryResponse(PaginationDto.of(slice), slice.getContent());
    }
}
