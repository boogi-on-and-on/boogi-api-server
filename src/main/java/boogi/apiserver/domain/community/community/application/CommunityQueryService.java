package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.request.CommunityQueryRequest;
import boogi.apiserver.domain.community.community.dto.response.CommunityMetadataDto;
import boogi.apiserver.domain.community.community.dto.response.CommunitySettingInfo;
import boogi.apiserver.domain.community.community.dto.response.SearchCommunityDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommunityQueryService {
    private final CommunityRepository communityRepository;

    public Community getCommunity(Long communityId) {
        Community community = communityRepository.findById(communityId).orElseThrow(EntityNotFoundException::new);
        return community;
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

    public CommunitySettingInfo getSettingInfo(Long communityId) {
        Community community = getCommunity(communityId);
        return CommunitySettingInfo.of(community);
    }
}
