package boogi.apiserver.domain.member.dto.response;

import boogi.apiserver.domain.community.community.dto.dto.JoinedMemberInfoDto;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.global.dto.PaginationDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JoinedMembersPageResponse {

    private List<JoinedMemberInfoDto> members;
    private PaginationDto pageInfo;

    public JoinedMembersPageResponse(List<JoinedMemberInfoDto> members, PaginationDto pageInfo) {
        this.members = members;
        this.pageInfo = pageInfo;
    }

    public static JoinedMembersPageResponse from(Slice<Member> memberPage) {
        List<JoinedMemberInfoDto> joinedMembers = memberPage.getContent()
                .stream()
                .map(JoinedMemberInfoDto::of)
                .collect(Collectors.toList());

        return new JoinedMembersPageResponse(joinedMembers, PaginationDto.of(memberPage));
    }
}
