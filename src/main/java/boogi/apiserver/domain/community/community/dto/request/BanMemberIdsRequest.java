package boogi.apiserver.domain.community.community.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
public class BanMemberIdsRequest {

    @NotNull
    private Long memberId;

    public BanMemberIdsRequest(final Long memberId) {
        this.memberId = memberId;
    }
}
