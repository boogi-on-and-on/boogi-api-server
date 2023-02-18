package boogi.apiserver.domain.user.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class BlockedUserIdRequest {
    private Long blockerUserId;

    @Builder
    public BlockedUserIdRequest(final Long blockerUserId) {
        this.blockerUserId = blockerUserId;
    }
}
