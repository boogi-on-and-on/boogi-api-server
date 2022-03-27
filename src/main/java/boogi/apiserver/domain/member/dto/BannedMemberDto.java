package boogi.apiserver.domain.member.dto;

import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.UserBasicProfileDto;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class BannedMemberDto {
    private Long memberId;
    private UserBasicProfileDto user;

    @QueryProjection
    public BannedMemberDto(Long memberId, User user) {
        this.memberId = memberId;
        this.user = UserBasicProfileDto.of(user);
    }
}
