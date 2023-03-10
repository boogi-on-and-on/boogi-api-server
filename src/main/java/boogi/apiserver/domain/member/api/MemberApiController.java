package boogi.apiserver.domain.member.api;

import boogi.apiserver.domain.community.community.dto.request.DelegateMemberRequest;
import boogi.apiserver.domain.member.application.MemberCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.dto.response.SearchMentionUsersResponse;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.global.argument_resolver.session.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/members")
public class MemberApiController {

    private final MemberCommandService memberCommandService;

    private final MemberQueryService memberQueryService;

    @GetMapping("/search/mention")
    public SearchMentionUsersResponse getMentionSearchMember(Pageable pageable,
                                                             @RequestParam Long communityId,
                                                             @RequestParam(required = false) String name) {
        Slice<UserBasicProfileDto> slice = memberQueryService.getMentionSearchMembers(pageable, communityId, name);
        return SearchMentionUsersResponse.from(slice);
    }

    @PostMapping("/{memberId}/ban")
    public void banMember(@Session Long userId, @PathVariable Long memberId) {
        memberCommandService.banMember(userId, memberId);
    }

    @PostMapping("/{memberId}/release")
    public void releaseBannedMember(@Session Long userId, @PathVariable Long memberId) {
        memberCommandService.releaseMember(userId, memberId);
    }

    @PostMapping("/{memberId}/delegate")
    public void delegateMember(@Session Long userId,
                               @PathVariable Long memberId,
                               @Validated @RequestBody DelegateMemberRequest request) {
        memberCommandService.delegeteMember(userId, memberId, request.getType());
    }
}
