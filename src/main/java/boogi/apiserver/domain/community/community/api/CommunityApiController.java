package boogi.apiserver.domain.community.community.api;

import boogi.apiserver.domain.community.community.application.CommunityCommandService;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.dto.dto.CommunityMetadataDto;
import boogi.apiserver.domain.community.community.dto.dto.CommunitySettingInfoDto;
import boogi.apiserver.domain.community.community.dto.dto.SearchCommunityDto;
import boogi.apiserver.domain.community.community.dto.dto.UserJoinRequestInfoDto;
import boogi.apiserver.domain.community.community.dto.request.*;
import boogi.apiserver.domain.community.community.dto.response.*;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestCommandService;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestQueryService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.dto.dto.BannedMemberDto;
import boogi.apiserver.domain.member.dto.response.JoinedMembersPageResponse;
import boogi.apiserver.domain.member.dto.response.JoinedMembersResponse;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.global.argument_resolver.session.Session;
import boogi.apiserver.global.dto.SimpleIdResponse;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/communities")
public class CommunityApiController {
    private final JoinRequestCommandService joinRequestCommandService;
    private final CommunityCommandService communityCommandService;

    private final CommunityQueryService communityQueryService;
    private final PostQueryService postQueryService;
    private final MemberQueryService memberQueryService;
    private final JoinRequestQueryService joinRequestQueryService;

    private final SendPushNotification sendPushNotification;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SimpleIdResponse createCommunity(@RequestBody @Validated CreateCommunityRequest request, @Session Long userId) {
        Long communityId = communityCommandService.createCommunity(request, userId);

        return SimpleIdResponse.from(communityId);
    }

    @GetMapping("/{communityId}")
    public CommunityDetailResponse getCommunityDetail(@Session Long userId, @PathVariable Long communityId) {
        return communityQueryService.getCommunityDetail(userId, communityId);
    }

    @GetMapping("/{communityId}/metadata")
    public CommunityMetaInfoResponse getCommunityMetadata(@Session Long userId, @PathVariable Long communityId) {
        CommunityMetadataDto metadata = communityQueryService.getCommunityMetadata(userId, communityId);

        return CommunityMetaInfoResponse.from(metadata);
    }

    @PatchMapping("/{communityId}")
    public void updateCommunity(@PathVariable Long communityId,
                                @Session Long userId,
                                @RequestBody @Validated UpdateCommunityRequest request) {
        communityCommandService.updateCommunity(userId, communityId, request.getDescription(), request.getHashtags());
    }

    @DeleteMapping("/{communityId}")
    public void shutdown(@PathVariable Long communityId, @Session Long userId) {
        communityCommandService.shutdown(userId, communityId);
    }

    @GetMapping("/{communityId}/settings")
    public UpdateCommunityResponse getSetting(@PathVariable Long communityId, @Session Long userId) {
        CommunitySettingInfoDto settingInfo = communityQueryService.getSetting(userId, communityId);

        return UpdateCommunityResponse.from(settingInfo);
    }

    @PostMapping("/{communityId}/settings")
    public void changeSetting(@PathVariable Long communityId,
                              @Session Long userId,
                              @RequestBody CommunitySettingRequest request) {
        communityCommandService.changeSetting(userId, communityId, request);
    }

    @GetMapping("/{communityId}/posts")
    public CommunityPostsResponse getPostsOfCommunity(@PathVariable Long communityId,
                                                      @Session Long userId,
                                                      Pageable pageable) {

        return postQueryService.getPostsOfCommunity(pageable, communityId, userId);

    }

    @GetMapping("/{communityId}/members")
    public JoinedMembersPageResponse getCommunityJoinedMembers(@PathVariable Long communityId, Pageable pageable) {
        Slice<Member> joinedMembers = memberQueryService.getCommunityJoinedMembers(pageable, communityId);
        return JoinedMembersPageResponse.from(joinedMembers);
    }

    @GetMapping("/{communityId}/members/banned")
    public BannedMembersResponse getBannedMembers(@Session Long userId, @PathVariable Long communityId) {
        List<BannedMemberDto> bannedMembers = memberQueryService.getBannedMembers(userId, communityId);

        return BannedMembersResponse.from(bannedMembers);
    }

    @GetMapping("/{communityId}/requests")
    public UserJoinRequestsResponse getCommunityJoinRequest(@Session Long userId, @PathVariable Long communityId) {
        List<UserJoinRequestInfoDto> requests = joinRequestQueryService.getAllRequests(userId, communityId);

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
        joinRequestCommandService.confirmUsers(managerUserId, requestIds, communityId);

        sendPushNotification.joinNotification(requestIds);
    }

    @PostMapping("/{communityId}/requests/reject")
    public void rejectRequest(@Session Long managerUserId,
                              @PathVariable Long communityId,
                              @Validated @RequestBody JoinRequestIdsRequest request
    ) {
        List<Long> requestIds = request.getRequestIds();
        joinRequestCommandService.rejectUsers(managerUserId, requestIds, communityId);

        sendPushNotification.rejectNotification(requestIds);
    }

    @GetMapping("/search")
    public CommunityQueryResponse searchCommunities(@ModelAttribute @Validated CommunityQueryRequest request,
                                                    Pageable pageable) {
        Slice<SearchCommunityDto> communities = communityQueryService.getSearchedCommunities(pageable, request);
        return CommunityQueryResponse.from(communities);
    }

    @GetMapping("{communityId}/members/all")
    public JoinedMembersResponse getMembersAll(@PathVariable Long communityId, @Session Long userId) {
        List<Member> joinedMembersAll = memberQueryService.getJoinedMembersAll(communityId, userId);
        return JoinedMembersResponse.from(joinedMembersAll);
    }
}
