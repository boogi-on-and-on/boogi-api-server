package boogi.apiserver.domain.member.dto.dto;

import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import lombok.Getter;

@Getter
public class MemberDto {
    private Long id;
    private MemberType type;
    private UserBasicProfileDto user;

    public MemberDto(final Long id, final MemberType type, final UserBasicProfileDto user) {
        this.id = id;
        this.type = type;
        this.user = user;
    }

    public static MemberDto of(Member member) {
        return new MemberDto(member.getId(), member.getMemberType(), UserBasicProfileDto.of(member.getUser()));
    }
}