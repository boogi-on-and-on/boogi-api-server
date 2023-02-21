package boogi.apiserver.domain.community.community.dto.response;


import boogi.apiserver.domain.member.dto.dto.BannedMemberDto;
import lombok.Getter;

import java.util.List;

@Getter
public class BannedMembersResponse {

    private final List<BannedMemberDto> banned;

    public BannedMembersResponse(List<BannedMemberDto> banned) {
        this.banned = banned;
    }

    public static BannedMembersResponse from(List<BannedMemberDto> banned) {
        return new BannedMembersResponse(banned);
    }
}
