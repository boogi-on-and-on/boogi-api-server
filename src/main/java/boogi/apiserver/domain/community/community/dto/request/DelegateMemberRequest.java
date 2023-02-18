package boogi.apiserver.domain.community.community.dto.request;


import boogi.apiserver.domain.member.domain.MemberType;
import lombok.*;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
public class DelegateMemberRequest {

    @NotNull
    private Long memberId;

    @NotNull
    private MemberType type;

    public DelegateMemberRequest(final Long memberId, final MemberType type) {
        this.memberId = memberId;
        this.type = type;
    }
}
