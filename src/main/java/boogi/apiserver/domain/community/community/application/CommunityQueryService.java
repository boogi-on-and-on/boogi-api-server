package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.dto.CommunityMetadataDto;
import boogi.apiserver.domain.community.community.dto.dto.CommunitySettingInfoDto;
import boogi.apiserver.domain.community.community.dto.dto.JoinedCommunitiesDto;
import boogi.apiserver.domain.community.community.dto.dto.SearchCommunityDto;
import boogi.apiserver.domain.community.community.dto.request.CommunityQueryRequest;
import boogi.apiserver.domain.community.community.dto.response.CommunityDetailResponse;
import boogi.apiserver.domain.community.community.dto.response.CommunityQueryResponse;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.dto.dto.NoticeDto;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.dto.LatestCommunityPostDto;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.user.dao.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommunityQueryService {
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;

    private final MemberQueryService memberQueryService;
    private final NoticeQueryService noticeQueryService;
    private final PostQueryService postQueryService;

    public Community getCommunityWithHashTag(Long communityId) {
        Community community = communityRepository.findByCommunityId(communityId);
        community.getHashtags().getValues().size(); //LAZY INIT

        return community;
    }

    public CommunityDetailResponse getCommunityDetail(Long userId, Long communityId) {
        Community community = communityRepository.findByCommunityId(communityId);
        Member member = memberQueryService.getMemberOrNullMember(userId, community);

        List<NoticeDto> communityNotices = noticeQueryService.getCommunityLatestNotice(communityId);
        List<LatestCommunityPostDto> latestPosts = postQueryService.getLatestPostOfCommunity(member, community);

        return CommunityDetailResponse.of(communityNotices, latestPosts, member, community);
    }

    public CommunityMetadataDto getCommunityMetadata(Long userId, Long communityId) {
        memberQueryService.getManager(userId, communityId);
        Community community = getCommunityWithHashTag(communityId);
        return CommunityMetadataDto.of(community);
    }

    public CommunityQueryResponse getSearchedCommunities(Pageable pageable, CommunityQueryRequest request) {
        Slice<SearchCommunityDto> communities = communityRepository.getSearchedCommunities(pageable, request);
        return CommunityQueryResponse.from(communities);
    }

    public CommunitySettingInfoDto getSetting(Long userId, Long communityId) {
        memberQueryService.getManager(userId, communityId);

        Community community = communityRepository.findByCommunityId(communityId);
        return CommunitySettingInfoDto.of(community);
    }

    public JoinedCommunitiesDto getJoinedCommunitiesWithLatestPost(Long userId) {
        userRepository.findByUserId(userId);

        Map<Long, Community> joinedCommunityMap = getJoinedCommunityMap(userId);

        List<Post> latestPosts = postRepository.getLatestPostByCommunityIds(joinedCommunityMap.keySet());
        List<Long> latestPostIds = latestPosts.stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        List<PostMedia> postMedias = postMediaRepository.getPostMediasByLatestPostIds(latestPostIds);

        return JoinedCommunitiesDto.of(joinedCommunityMap, latestPosts, postMedias);
    }

    private LinkedHashMap<Long, Community> getJoinedCommunityMap(Long userId) {
        List<Member> findMembers = memberRepository.findWhatIJoined(userId);
        return findMembers.stream()
                .map(Member::getCommunity)
                .collect(Collectors.toMap(
                        m1 -> m1.getId(),
                        m2 -> m2,
                        (o, n) -> n,
                        LinkedHashMap::new
                ));
    }
}
