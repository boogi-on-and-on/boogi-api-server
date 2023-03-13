package boogi.apiserver.domain.community.community.dao;

import boogi.apiserver.domain.community.community.dto.dto.SearchCommunityDto;
import boogi.apiserver.domain.community.community.dto.request.CommunityQueryRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface CommunityRepositoryCustom {
    Slice<SearchCommunityDto> getSearchedCommunities(Pageable pageable, CommunityQueryRequest condition);
}
