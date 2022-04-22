package boogi.apiserver.domain.community.community.dao;

import boogi.apiserver.domain.community.community.dto.CommunityQueryRequest;
import boogi.apiserver.domain.community.community.dto.SearchCommunityDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommunityRepositoryCustom {
    Page<SearchCommunityDto> getSearchedCommunities(Pageable pageable, CommunityQueryRequest condition);
}
