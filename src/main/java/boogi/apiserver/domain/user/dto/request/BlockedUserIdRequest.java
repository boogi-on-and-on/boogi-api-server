package boogi.apiserver.domain.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BlockedUserIdRequest {
    private Long blockerUserId;

    public BlockedUserIdRequest(final Long blockerUserId) {
        this.blockerUserId = blockerUserId;
    }
}
