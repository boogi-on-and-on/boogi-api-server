package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.request.CommunityQueryRequest;
import boogi.apiserver.domain.community.community.dto.response.CommunityMetadataDto;
import boogi.apiserver.domain.community.community.dto.response.SearchCommunityDto;
import boogi.apiserver.domain.community.community.exception.CommunityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommunityQueryService {
    private final CommunityRepository communityRepository;

    public Community getCommunity(Long communityId) {
        if (communityId == null) {
            throw new IllegalArgumentException("communityId는 null일 수 없습니다.");
        }
        return communityRepository.findById(communityId)
                .orElseThrow(CommunityNotFoundException::new);
    }

    public Community getCommunityWithHashTag(Long communityId) {
        Community community = this.getCommunity(communityId);
        community.getHashtags().size(); //LAZY INIT

        return community;
    }

    public CommunityMetadataDto getCommunityMetadata(Long communityId) {
        Community community = this.getCommunityWithHashTag(communityId);

        return CommunityMetadataDto.of(community);
    }

    public Slice<SearchCommunityDto> getSearchedCommunities(Pageable pageable, CommunityQueryRequest request) {
        return communityRepository.getSearchedCommunities(pageable, request);
    }
}
