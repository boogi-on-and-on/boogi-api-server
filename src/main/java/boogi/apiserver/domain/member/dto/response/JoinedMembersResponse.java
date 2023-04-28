package boogi.apiserver.domain.member.dto.response;

import boogi.apiserver.domain.member.dto.dto.MemberDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JoinedMembersResponse {

    private List<MemberDto> members;

    public JoinedMembersResponse(List<MemberDto> memberDtos) {
        this.members = memberDtos;
    }

    public static JoinedMembersResponse from(List<MemberDto> members) {
        return new JoinedMembersResponse(members);
    }
}
