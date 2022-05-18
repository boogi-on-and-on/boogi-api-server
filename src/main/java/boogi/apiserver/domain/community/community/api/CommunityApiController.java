package boogi.apiserver.domain.community.community.api;

import boogi.apiserver.domain.community.community.application.CommunityCoreService;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.*;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestCoreService;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestQueryService;
import boogi.apiserver.domain.member.application.MemberCoreService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.dto.BannedMemberDto;
import boogi.apiserver.domain.member.dto.JoinedMembersPageDto;
import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.dto.NoticeDto;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.LatestPostOfCommunityDto;
import boogi.apiserver.domain.post.post.dto.PostOfCommunity;
import boogi.apiserver.domain.user.dto.UserBasicProfileDto;
import boogi.apiserver.global.argument_resolver.session.Session;
import boogi.apiserver.global.dto.PagnationDto;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/communities")
public class CommunityApiController {

    private final JoinRequestCoreService joinRequestCoreService;
    private final CommunityCoreService communityCoreService;
    private final MemberCoreService memberCoreService;

    private final MemberValidationService memberValidationService;

    private final CommunityQueryService communityQueryService;
    private final NoticeQueryService noticeQueryService;
    private final PostQueryService postQueryService;
    private final MemberQueryService memberQueryService;
    private final JoinRequestQueryService joinRequestQueryService;

    @PostMapping
    public ResponseEntity<Object> createCommunity(@RequestBody @Validated CreateCommunityRequest request, @Session Long userId) {
        Community community = Community.of(request.getName(), request.getDescription(), request.getIsPrivate(), request.getAutoApproval());
        Long communityId = communityCoreService.createCommunity(community, request.getHashtags(), userId).getId();

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "communityId", communityId
        ));
    }

    @GetMapping("/{communityId}")
    public ResponseEntity<Object> getCommunityDetailInfo(@Session Long userId, @PathVariable Long communityId) {
        Member member = memberQueryService.getMemberOfTheCommunity(userId, communityId);
        Community community = communityQueryService.getCommunityWithHashTag(communityId);
        CommunityDetailInfoDto communityDetailInfoWithMember = CommunityDetailInfoDto.of(community);

        List<NoticeDto> communityNotices = noticeQueryService.getCommunityLatestNotice(communityId)
                .stream()
                .map(NoticeDto::of)
                .collect(Collectors.toList());

        HashMap<String, Object> response = new HashMap<>(Map.of(
                "isJoined", member != null,
                "community", communityDetailInfoWithMember,
                "notices", communityNotices));

        boolean showPostList = !(Objects.isNull(member) && community.isPrivate());
        if (showPostList) {
            List<LatestPostOfCommunityDto> latestPosts = postQueryService.getLatestPostOfCommunity(communityId)
                    .stream()
                    .map(LatestPostOfCommunityDto::of)
                    .collect(Collectors.toList());
            response.put("posts", latestPosts);
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{communityId}/metadata")
    public ResponseEntity<Object> getCommunityMetadata(@Session Long userId, @PathVariable Long communityId) {
        // aop 이용해서 권한 체크하기?
        memberValidationService.hasAuth(userId, communityId, MemberType.MANAGER);

        CommunityMetadataDto metadata = communityQueryService.getCommunityMetadata(communityId);

        return ResponseEntity.ok(Map.of(
                "metadata", metadata
        ));
    }

    @PatchMapping("/{communityId}")
    public ResponseEntity<Object> updateCommunityInfo(@PathVariable Long communityId,
                                                      @Session Long userId,
                                                      @RequestBody @Validated CommunityUpdateRequest request) {

        // aop 이용해서 권한 체크하기?
        memberValidationService.hasAuth(userId, communityId, MemberType.MANAGER);

        communityCoreService.update(communityId, request.getDescription(), request.getHashtags());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{communityId}")
    public ResponseEntity<Object> shutdown(@PathVariable Long communityId, @Session Long userId) {
        // aop 이용해서 권한 체크하기?
        memberValidationService.hasAuth(userId, communityId, MemberType.MANAGER);

        communityCoreService.shutdown(communityId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{communityId}/settings")
    public ResponseEntity<Object> getSettingInfo(@PathVariable Long communityId, @Session Long userId) {
        // aop 이용해서 권한 체크하기?
        memberValidationService.hasAuth(userId, communityId, MemberType.MANAGER);

        Community community = communityQueryService.getCommunity(communityId);

        return ResponseEntity.ok(Map.of(
                "settingInfo", CommunitySettingInfo.of(community)
        ));
    }

    @PostMapping("/{communityId}/settings")
    public ResponseEntity<Void> setting(@PathVariable Long communityId,
                                        @Session Long userId,
                                        @RequestBody CommunitySettingRequest request
    ) {
        // aop 이용해서 권한 체크하기?
        memberValidationService.hasAuth(userId, communityId, MemberType.MANAGER);

        Boolean isAuto = request.getIsAutoApproval();
        Boolean isSecret = request.getIsSecret();
        if (Objects.nonNull(isAuto)) {
            communityCoreService.changeApproval(communityId, isAuto);
        }
        if (Objects.nonNull(isSecret)) {
            communityCoreService.changeScope(communityId, isSecret);
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{communityId}/posts")
    public ResponseEntity<Object> getPosts(@PathVariable Long communityId,
                                           @Session Long userId,
                                           Pageable pageable
    ) {
        Member member = memberQueryService.getMemberOfTheCommunity(userId, communityId);
        Community community = communityQueryService.getCommunity(communityId);

        boolean unauthorized = Objects.isNull(member) && community.isPrivate();
        if (unauthorized) {
            throw new InvalidValueException("비공개 커뮤니티이면서, 가입되지 않았습니다.");
        }

        Page<Post> postPage = postQueryService.getPostsOfCommunity(pageable, communityId);
        List<PostOfCommunity> posts = postPage.getContent()
                .stream()
                .map(p -> new PostOfCommunity(p, userId))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "communityName", community.getCommunityName(),
                "memberType", member.getMemberType(),
                "posts", posts,
                "pageInfo", new PagnationDto(postPage)
        ));
    }

    @GetMapping("/{communityId}/members")
    public ResponseEntity<JoinedMembersPageDto> getMembers(@PathVariable Long communityId, Pageable pageable) {
        Page<Member> members = memberQueryService.getCommunityJoinedMembers(pageable, communityId);
        return ResponseEntity.status(HttpStatus.OK).body(JoinedMembersPageDto.of(members));
    }

    @GetMapping("/{communityId}/members/banned")
    public ResponseEntity<Object> getBannedMembers(@Session Long userId, @PathVariable Long communityId) {
        // aop 이용해서 권한 체크하기?
        memberValidationService.hasAuth(userId, communityId, MemberType.SUB_MANAGER);

        List<BannedMemberDto> bannedMembers = memberQueryService.getBannedMembers(communityId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "banned", bannedMembers
        ));
    }

    @PostMapping("/{communityId}/members/ban")
    public ResponseEntity<Object> banMember(@Session Long userId,
                                            @PathVariable Long communityId,
                                            @RequestBody HashMap<String, Long> body) {
        // aop 이용해서 권한 체크하기?
        memberValidationService.hasAuth(userId, communityId, MemberType.SUB_MANAGER);

        Long banMemberId = body.get("memberId");
        memberCoreService.banMember(banMemberId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{communityId}/members/release")
    public ResponseEntity<Object> releaseBannedMember(@Session Long userId,
                                                      @PathVariable Long communityId,
                                                      @RequestBody HashMap<String, Long> request
    ) {
        // aop 이용해서 권한 체크하기?
        memberValidationService.hasAuth(userId, communityId, MemberType.MANAGER);

        Long memberId = request.get("memberId");
        memberCoreService.releaseMember(memberId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{communityId}/members/delegate")
    public ResponseEntity<Object> delegateMember(@Session Long userId,
                                                 @PathVariable Long communityId,
                                                 @RequestBody DelegateMemberRequest request
    ) {
        // aop 이용해서 권한 체크하기?
        memberValidationService.hasAuth(userId, communityId, MemberType.MANAGER);

        memberCoreService.delegeteMember(request.getMemberId(), request.getType());

        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @GetMapping("/{communityId}/requests")
    public ResponseEntity<Object> getCommunityJoinRequest(@Session Long userId, @PathVariable Long communityId) {
        // aop 이용해서 권한 체크하기?
        memberValidationService.hasAuth(userId, communityId, MemberType.SUB_MANAGER);

        List<Map<String, Object>> requests = joinRequestQueryService.getAllRequests(communityId)
                .stream()
                .map(r -> Map.of(
                        "user", UserBasicProfileDto.of(r.getUser()),
                        "id", r.getId())
                )
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "requests", requests
        ));
    }

    @PostMapping("/{communityId}/requests")
    public ResponseEntity<Object> joinRequest(@Session Long userId, @PathVariable Long communityId) {
        Long requestId = joinRequestCoreService.request(userId, communityId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "requestId", requestId
        ));
    }

    @PostMapping("/{communityId}/requests/confirm")
    public ResponseEntity<Object> confirmRequest(@Session Long managerUserId,
                                                 @PathVariable Long communityId,
                                                 @RequestBody HashMap<String, List<Long>> body
    ) {
        List<Long> requestIds = body.get("requestIds");

        // aop 이용해서 권한 체크하기?
        memberValidationService.hasAuth(managerUserId, communityId, MemberType.SUB_MANAGER);

        joinRequestCoreService.confirmUserInBatch(managerUserId, requestIds, communityId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{communityId}/requests/reject")
    public ResponseEntity<Object> rejectRequest(@Session Long managerUserId,
                                                @PathVariable Long communityId,
                                                @RequestBody HashMap<String, List<Long>> body
    ) {
        List<Long> requestIds = body.get("requestIds");

        // aop 이용해서 권한 체크하기?
        memberValidationService.hasAuth(managerUserId, communityId, MemberType.SUB_MANAGER);

        joinRequestCoreService.rejectUserInBatch(managerUserId, requestIds, communityId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchCommunities(@ModelAttribute @Validated CommunityQueryRequest request,
                                                    Pageable pageable) {
        Page<SearchCommunityDto> page = communityQueryService.getSearchedCommunities(pageable, request);

        return ResponseEntity.ok(Map.of(
                "communities", page.getContent(),
                "pageInfo", PagnationDto.of(page)
        ));
    }
}
