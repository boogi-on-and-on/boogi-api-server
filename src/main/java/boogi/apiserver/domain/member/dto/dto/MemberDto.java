package boogi.apiserver.domain.member.dto.dto;

import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MemberDto {
    private Long id;
    private MemberType type;
    private UserBasicProfileDto user;

    public MemberDto(Long id, MemberType type, UserBasicProfileDto user) {
        this.id = id;
        this.type = type;
        this.user = user;
    }

    public static MemberDto of(Member member) {
        return new MemberDto(member.getId(), member.getMemberType(), UserBasicProfileDto.from(member.getUser()));
    }

    public static List<MemberDto> listOf(List<Member> members) {
        return members.stream()
                .map(MemberDto::of)
                .collect(Collectors.toList());
    }
}