package boogi.apiserver.domain.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BlockedUserIdRequest {
    private Long blockedUserId;

    public BlockedUserIdRequest(Long blockedUserId) {
        this.blockedUserId = blockedUserId;
    }
}
