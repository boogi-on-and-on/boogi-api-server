package boogi.apiserver.domain.community.community.api;

import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.application.CommunityCommandService;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.dto.dto.CommunityMetadataDto;
import boogi.apiserver.domain.community.community.dto.dto.CommunitySettingInfoDto;
import boogi.apiserver.domain.community.community.dto.dto.SearchCommunityDto;
import boogi.apiserver.domain.community.community.dto.dto.UserJoinRequestInfoDto;
import boogi.apiserver.domain.community.community.dto.request.*;
import boogi.apiserver.domain.community.community.dto.response.*;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestQueryService;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.application.MemberCommandService;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.dto.dto.BannedMemberDto;
import boogi.apiserver.domain.member.dto.dto.MemberDto;
import boogi.apiserver.domain.member.dto.response.JoinedMembersResponse;
import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.dto.dto.NoticeDto;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.dto.LatestCommunityPostDto;
import boogi.apiserver.global.argument_resolver.session.Session;
import boogi.apiserver.global.dto.SimpleIdResponse;
import boogi.apiserver.global.error.exception.InvalidValueException;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/communities")
public class CommunityApiController {
    private final CommunityRepository communityRepository;

    private final JoinRequestCommandService joinRequestCommandService;
    private final CommunityCommandService communityCommandService;
    private final MemberCommandService memberCommandService;

    private final MemberValidationService memberValidationService;

    private final CommunityQueryService communityQueryService;
    private final NoticeQueryService noticeQueryService;
    private final PostQueryService postQueryService;
    private final MemberQueryService memberQueryService;
    private final JoinRequestQueryService joinRequestQueryService;

    private final SendPushNotification sendPushNotification;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SimpleIdResponse createCommunity(@RequestBody @Validated CreateCommunityRequest request, @Session Long userId) {
        String _category = request.getCategory();
        CommunityCategory category = CommunityCategory.valueOf(_category);
        Community community = Community.of(request.getName(), request.getDescription(), request.getIsPrivate(), request.getAutoApproval(), category);
        Long communityId = communityCommandService.createCommunity(community, request.getHashtags(), userId).getId();

        return SimpleIdResponse.from(communityId);
    }

    @GetMapping("/{communityId}")
    public CommunityDetailResponse getCommunityDetailInfo(@Session Long userId, @PathVariable Long communityId) {
        Member member = memberQueryService.getMemberOfTheCommunity(userId, communityId);
        Community community = communityQueryService.getCommunityWithHashTag(communityId);

        List<NoticeDto> communityNotices = noticeQueryService.getCommunityLatestNotice(communityId);

        boolean showPostList = !(Objects.isNull(member) && community.isPrivate());
        List<LatestCommunityPostDto> latestPosts = (showPostList == false) ? null :
                postQueryService.getLatestPostOfCommunity(communityId);

        return CommunityDetailResponse.of(communityNotices, latestPosts, member, community);
    }

    @GetMapping("/{communityId}/metadata")
    public CommunityMetaInfoResponse getCommunityMetadata(@Session Long userId, @PathVariable Long communityId) {
        memberValidationService.hasAuth(userId, communityId, MemberType.MANAGER);

        CommunityMetadataDto metadata = communityQueryService.getCommunityMetadata(communityId);

        return CommunityMetaInfoResponse.from(metadata);
    }

    @PatchMapping("/{communityId}")
    public void updateCommunityInfo(@PathVariable Long communityId,
                                    @Session Long userId,
                                    @RequestBody @Validated UpdateCommunityRequest request) {
        memberValidationService.hasAuth(userId, communityId, MemberType.MANAGER);

        communityCommandService.update(communityId, request.getDescription(), request.getHashtags());
    }

    @DeleteMapping("/{communityId}")
    public void shutdown(@PathVariable Long communityId, @Session Long userId) {
        memberValidationService.hasAuth(userId, communityId, MemberType.MANAGER);

        communityCommandService.shutdown(communityId);
    }

    @GetMapping("/{communityId}/settings")
    public UpdateCommunityResponse getSettingInfo(@PathVariable Long communityId, @Session Long userId) {
        memberValidationService.hasAuth(userId, communityId, MemberType.MANAGER);

        CommunitySettingInfoDto settingInfo = communityQueryService.getSettingInfo(communityId);

        return UpdateCommunityResponse.from(settingInfo);
    }

    @PostMapping("/{communityId}/settings")
    public void setting(@PathVariable Long communityId,
                        @Session Long userId,
                        @RequestBody CommunitySettingRequest request
    ) {
        memberValidationService.hasAuth(userId, communityId, MemberType.MANAGER);

        Boolean isAuto = request.getIsAutoApproval();
        Boolean isSecret = request.getIsSecret();
        if (Objects.nonNull(isAuto)) {
            communityCommandService.changeApproval(communityId, isAuto);
        }
        if (Objects.nonNull(isSecret)) {
            communityCommandService.changeScope(communityId, isSecret);
        }
    }

    @GetMapping("/{communityId}/posts")
    public CommunityPostsResponse getPosts(@PathVariable Long communityId,
                                           @Session Long userId,
                                           Pageable pageable
    ) {
        Member member = memberQueryService.getMemberOfTheCommunity(userId, communityId);
        Community community = communityRepository.findByCommunityId(communityId);

        //todo: 아래 로직 ValidationService로 이동
        boolean unauthorized = Objects.isNull(member) && community.isPrivate();
        if (unauthorized) {
            throw new InvalidValueException("비공개 커뮤니티이면서, 가입되지 않았습니다.");
        }

        Slice<Post> postPage = postQueryService.getPostsOfCommunity(pageable, communityId);

        return CommunityPostsResponse.of(community.getCommunityName(), userId, postPage, member);
    }

    //todo: 추후 Repository에서 Dto로 Projection하는 것 수정할 때 같이 하기
//    @GetMapping("/{communityId}/members")
//    public JoinedMembersPageResponse getMembers(@PathVariable Long communityId, Pageable pageable) {
//        Slice<Member> members = memberQueryService.getCommunityJoinedMembers(pageable, communityId);
//
//        return JoinedMembersPageResponse.from(members);
//    }

    @GetMapping("/{communityId}/members/banned")
    public BannedMembersResponse getBannedMembers(@Session Long userId, @PathVariable Long communityId) {
        memberValidationService.hasAuth(userId, communityId, MemberType.SUB_MANAGER);

        List<BannedMemberDto> bannedMembers = memberQueryService.getBannedMembers(communityId);

        return BannedMembersResponse.from(bannedMembers);
    }

    @PostMapping("/{communityId}/members/ban")
    public void banMember(@Session Long userId,
                          @PathVariable Long communityId,
                          @Validated @RequestBody BanMemberIdsRequest request) {
        memberValidationService.hasAuth(userId, communityId, MemberType.SUB_MANAGER);

        Long banMemberId = request.getMemberId();
        memberCommandService.banMember(banMemberId);
    }

    @PostMapping("/{communityId}/members/release")
    public void releaseBannedMember(@Session Long userId,
                                    @PathVariable Long communityId,
                                    @Validated @RequestBody BanMemberIdsRequest request
    ) {
        memberValidationService.hasAuth(userId, communityId, MemberType.MANAGER);

        Long memberId = request.getMemberId();
        memberCommandService.releaseMember(memberId);
    }

    @PostMapping("/{communityId}/members/delegate")
    public void delegateMember(@Session Long userId,
                               @PathVariable Long communityId,
                               @Validated @RequestBody DelegateMemberRequest request
    ) {
        memberCommandService.delegeteMember(userId, request.getMemberId(), request.getType());
    }

    @GetMapping("/{communityId}/requests")
    public UserJoinRequestsResponse getCommunityJoinRequest(@Session Long userId, @PathVariable Long communityId) {
        memberValidationService.hasAuth(userId, communityId, MemberType.SUB_MANAGER);

        List<UserJoinRequestInfoDto> requests = joinRequestQueryService.getAllRequests(communityId);

        return UserJoinRequestsResponse.from(requests);
    }

    @PostMapping("/{communityId}/requests")
    public SimpleIdResponse joinRequest(@Session Long userId, @PathVariable Long communityId) {
        Long requestId = joinRequestCommandService.request(userId, communityId);

        return SimpleIdResponse.from(requestId);
    }

    @PostMapping("/{communityId}/requests/confirm")
    public void confirmRequest(@Session Long managerUserId,
                               @PathVariable Long communityId,
                               @Validated @RequestBody JoinRequestIdsRequest request
    ) {
        List<Long> requestIds = request.getRequestIds();

        memberValidationService.hasAuth(managerUserId, communityId, MemberType.SUB_MANAGER);

        joinRequestCommandService.confirmUserInBatch(managerUserId, requestIds, communityId);

        sendPushNotification.joinNotification(requestIds);
    }

    @PostMapping("/{communityId}/requests/reject")
    public void rejectRequest(@Session Long managerUserId,
                              @PathVariable Long communityId,
                              @Validated @RequestBody JoinRequestIdsRequest request
    ) {
        List<Long> requestIds = request.getRequestIds();

        memberValidationService.hasAuth(managerUserId, communityId, MemberType.SUB_MANAGER);

        joinRequestCommandService.rejectUserInBatch(managerUserId, requestIds, communityId);

        sendPushNotification.rejectNotification(requestIds);
    }

    @GetMapping("/search")
    public CommunityQueryResponse searchCommunities(@ModelAttribute @Validated CommunityQueryRequest request,
                                                    Pageable pageable) {
        Slice<SearchCommunityDto> slice = communityQueryService.getSearchedCommunities(pageable, request);

        return CommunityQueryResponse.from(slice);
    }

    @GetMapping("{communityId}/members/all")
    public JoinedMembersResponse getMembersAll(@PathVariable Long communityId, @Session Long userId) {
        final List<MemberDto> joinedMembers = memberQueryService.getJoinedMembersAll(communityId, userId);
        return JoinedMembersResponse.of(joinedMembers);
    }
}
