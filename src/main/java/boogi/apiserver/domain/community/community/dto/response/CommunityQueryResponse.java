package boogi.apiserver.domain.community.community.dto.response;

import boogi.apiserver.domain.community.community.dto.dto.SearchCommunityDto;
import boogi.apiserver.global.dto.PaginationDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
public class CommunityQueryResponse {
    final PaginationDto pageInfo;
    final List<SearchCommunityDto> communities;

    @Builder(access = AccessLevel.PRIVATE)
    private CommunityQueryResponse(final PaginationDto pageInfo, final List<SearchCommunityDto> communities) {
        this.pageInfo = pageInfo;
        this.communities = communities;
    }

    public static CommunityQueryResponse of(final Slice<SearchCommunityDto> slice) {
        return CommunityQueryResponse.builder()
                .pageInfo(PaginationDto.of(slice))
                .communities(slice.getContent())
                .build();
    }
}
