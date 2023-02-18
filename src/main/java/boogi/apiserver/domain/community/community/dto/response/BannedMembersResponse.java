package boogi.apiserver.domain.community.community.dto.response;


import boogi.apiserver.domain.member.dto.dto.BannedMemberDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class BannedMembersResponse {

    final List<BannedMemberDto> banned;

    @Builder(access = AccessLevel.PRIVATE)
    private BannedMembersResponse(List<BannedMemberDto> banned) {
        this.banned = banned;
    }

    public static BannedMembersResponse from(List<BannedMemberDto> banned) {
        return BannedMembersResponse.builder()
                .banned(banned)
                .build();
    }
}
