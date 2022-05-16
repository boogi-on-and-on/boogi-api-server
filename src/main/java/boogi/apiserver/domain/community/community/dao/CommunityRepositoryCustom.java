package boogi.apiserver.domain.community.community.dao;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.CommunityQueryRequest;
import boogi.apiserver.domain.community.community.dto.SearchCommunityDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CommunityRepositoryCustom {
    Page<SearchCommunityDto> getSearchedCommunities(Pageable pageable, CommunityQueryRequest condition);

    Optional<Community> findCommunityById(Long communityId);


}
