package boogi.apiserver.domain.community.community.dto.request;


import boogi.apiserver.domain.member.domain.MemberType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
public class DelegateMemberRequest {

    @NotNull(message = "멤버 권한을 입력해주세요")
    private MemberType type;

    public DelegateMemberRequest(MemberType type) {
        this.type = type;
    }
}
