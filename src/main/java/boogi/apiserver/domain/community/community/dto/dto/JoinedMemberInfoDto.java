package boogi.apiserver.domain.community.community.dto.dto;

import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.response.UserDetailInfoResponse;
import lombok.Getter;

@Getter
public class JoinedMemberInfoDto {

    private Long id;
    private String memberType;
    private String createdAt;
    private UserDetailInfoResponse user;

    public JoinedMemberInfoDto(final Long id, final String memberType, final String createdAt, final UserDetailInfoResponse user) {
        this.id = id;
        this.memberType = memberType;
        this.createdAt = createdAt;
        this.user = user;
    }

    public static JoinedMemberInfoDto of(Member member, User user) {
        return new JoinedMemberInfoDto(member.getId(), member.getMemberType().toString(),
                member.getCreatedAt().toString(), UserDetailInfoResponse.of(user));
    }
}
