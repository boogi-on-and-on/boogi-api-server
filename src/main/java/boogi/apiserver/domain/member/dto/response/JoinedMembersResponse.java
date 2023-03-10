package boogi.apiserver.domain.member.dto.response;

import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.dto.dto.MemberDto;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class JoinedMembersResponse {

    private final List<MemberDto> members;

    public JoinedMembersResponse(List<MemberDto> memberDtos) {
        this.members = memberDtos;
    }

    public static JoinedMembersResponse from(List<Member> members) {
        List<MemberDto> memberDtos = MemberDto.listOf(members);
        return new JoinedMembersResponse(memberDtos);
    }
}
