package boogi.apiserver.domain.member.dto.dto;

import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

@NoArgsConstructor
@Getter
public class BannedMemberDto {
    private Long memberId;
    private UserBasicProfileDto user;

    @QueryProjection
    public BannedMemberDto(Long memberId, User user) {
        this.memberId = memberId;
        this.user = UserBasicProfileDto.from(user);
    }

    public BannedMemberDto(Long memberId, UserBasicProfileDto user) {
        this.memberId = memberId;
        this.user = user;
    }
}
