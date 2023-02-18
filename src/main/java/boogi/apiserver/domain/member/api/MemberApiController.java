package boogi.apiserver.domain.member.api;

import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.dto.response.SearchMentionUsersResponse;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/members")
public class MemberApiController {

    private final MemberQueryService memberQueryService;

    @GetMapping("/search/mention")
    public SearchMentionUsersResponse getMentionSearchMember(Pageable pageable,
                                                             @RequestParam Long communityId,
                                                             @RequestParam(required = false) String name) {
        Slice<UserBasicProfileDto> slice = memberQueryService.getMentionSearchMembers(pageable, communityId, name);
        return SearchMentionUsersResponse.from(slice);
    }
}
