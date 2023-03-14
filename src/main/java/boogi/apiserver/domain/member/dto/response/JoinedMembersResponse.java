package boogi.apiserver.domain.member.dto.response;

import boogi.apiserver.domain.member.dto.dto.MemberDto;
import lombok.Getter;

import java.util.List;

@Getter
public class JoinedMembersResponse {

    private final List<MemberDto> members;

    public JoinedMembersResponse(List<MemberDto> memberDtos) {
        this.members = memberDtos;
    }

    public static JoinedMembersResponse from(List<MemberDto> members) {
        return new JoinedMembersResponse(members);
    }
}
