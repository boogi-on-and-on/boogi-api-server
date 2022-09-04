package boogi.apiserver.domain.member.dto.response;

import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.dto.response.UserBasicProfileDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class JoinedMembersDto {

    private List<MemberDto> members;

    @Getter
    @Builder
    static class MemberDto {
        private Long id;
        private MemberType type;
        private UserBasicProfileDto user;

        public static MemberDto of(Member member) {
            return MemberDto.builder()
                    .id(member.getId())
                    .type(member.getMemberType())
                    .user(UserBasicProfileDto.of(member.getUser()))
                    .build();
        }
    }

    private JoinedMembersDto(List<Member> members) {
        this.members = members.stream()
                .map(m -> MemberDto.of(m))
                .collect(Collectors.toList());
    }

    public static JoinedMembersDto of(List<Member> members) {
        return new JoinedMembersDto(members);
    }
}
