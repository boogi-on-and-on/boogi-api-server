package boogi.apiserver.domain.community.community.api;

import boogi.apiserver.domain.community.community.application.CommunityCoreService;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.CommunityDetailInfoDto;
import boogi.apiserver.domain.community.community.dto.CreateCommunityRequest;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.dto.NoticeDto;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.dto.LatestPostOfCommunityDto;
import boogi.apiserver.global.argument_resolver.session.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final CommunityCoreService communityCoreService;

    private final CommunityQueryService communityQueryService;
    private final NoticeQueryService noticeQueryService;
    private final PostQueryService postQueryService;
    private final MemberQueryService memberQueryService;

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
}
