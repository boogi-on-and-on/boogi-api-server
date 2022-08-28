package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.response.CommunityMetadataDto;
import boogi.apiserver.domain.community.community.dto.request.CommunityQueryRequest;
import boogi.apiserver.domain.community.community.dto.response.SearchCommunityDto;
import boogi.apiserver.domain.member.dao.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommunityQueryService {
    private final CommunityRepository communityRepository;
    private final MemberRepository memberRepository;


    public Community getCommunity(Long communityId) {
        Community community = communityRepository.findById(communityId).orElseThrow(EntityNotFoundException::new);
        return community;
    }

    public Community getCommunityWithHashTag(Long communityId) {
        Community community = this.getCommunity(communityId);
        community.getHashtags().size(); //LAZY INIT

        return community;
    }

    //todo: queryService에서 dto 만들도록 다른 API 수정 (OSIV off라서, LAZY INIT을 Service에서 한다)
    public CommunityMetadataDto getCommunityMetadata(Long communityId) {
        Community community = this.getCommunityWithHashTag(communityId);

        return CommunityMetadataDto.of(community);
    }

    public Page<SearchCommunityDto> getSearchedCommunities(Pageable pageable, CommunityQueryRequest request) {
        return communityRepository.getSearchedCommunities(pageable, request);
    }
}
