package boogi.apiserver.domain.community.community.dto.response;


import boogi.apiserver.domain.member.dto.dto.BannedMemberDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BannedMembersResponse {

    private List<BannedMemberDto> banned;

    public BannedMembersResponse(List<BannedMemberDto> banned) {
        this.banned = banned;
    }

    public static BannedMembersResponse from(List<BannedMemberDto> banned) {
        return new BannedMembersResponse(banned);
    }
}
