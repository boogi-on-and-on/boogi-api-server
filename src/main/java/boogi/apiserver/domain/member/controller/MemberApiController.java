package boogi.apiserver.domain.member.controller;

import boogi.apiserver.domain.community.community.dto.request.DelegateMemberRequest;
import boogi.apiserver.domain.member.application.MemberCommand;
import boogi.apiserver.domain.member.application.MemberQuery;
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

    private final MemberCommand memberCommand;

    private final MemberQuery memberQuery;

    @GetMapping("/search/mention")
    public SearchMentionUsersResponse getMentionSearchMember(Pageable pageable,
                                                             @RequestParam Long communityId,
                                                             @RequestParam(required = false) String name) {
        Slice<UserBasicProfileDto> slice = memberQuery.getMentionSearchMembers(pageable, communityId, name);
        return SearchMentionUsersResponse.from(slice);
    }

    @PostMapping("/{memberId}/ban")
    public void banMember(@Session Long userId, @PathVariable Long memberId) {
        memberCommand.banMember(userId, memberId);
    }

    @PostMapping("/{memberId}/release")
    public void releaseBannedMember(@Session Long userId, @PathVariable Long memberId) {
        memberCommand.releaseMember(userId, memberId);
    }

    @PostMapping("/{memberId}/delegate")
    public void delegateMember(@Session Long userId,
                               @PathVariable Long memberId,
                               @Validated @RequestBody DelegateMemberRequest request) {
        memberCommand.delegateMember(userId, memberId, request.getType());
    }
}
