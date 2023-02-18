package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.request.CommunityQueryRequest;
import boogi.apiserver.domain.community.community.dto.dto.CommunityMetadataDto;
import boogi.apiserver.domain.community.community.dto.dto.CommunitySettingInfoDto;
import boogi.apiserver.domain.community.community.dto.dto.SearchCommunityDto;
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

    public Community getCommunityWithHashTag(Long communityId) {
        Community community = communityRepository.findByCommunityId(communityId);
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

    public CommunitySettingInfoDto getSettingInfo(Long communityId) {
        Community community = communityRepository.findByCommunityId(communityId);
        return CommunitySettingInfoDto.of(community);
    }
}
