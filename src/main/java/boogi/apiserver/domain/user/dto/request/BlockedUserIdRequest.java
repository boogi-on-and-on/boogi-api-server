package boogi.apiserver.domain.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class BlockedUserIdRequest {

    @NotNull(message = "차단 해제할 유저를 선택해주세요")
    private Long blockedUserId;

    public BlockedUserIdRequest(Long blockedUserId) {
        this.blockedUserId = blockedUserId;
    }
}
