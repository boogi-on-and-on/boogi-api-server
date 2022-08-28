package boogi.apiserver.domain.community.community.dao;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.request.CommunityQueryRequest;
import boogi.apiserver.domain.community.community.dto.response.SearchCommunityDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Optional;

public interface CommunityRepositoryCustom {
    Slice<SearchCommunityDto> getSearchedCommunities(Pageable pageable, CommunityQueryRequest condition);

    Optional<Community> findCommunityById(Long communityId);


}
