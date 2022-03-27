package boogi.apiserver.domain.community.community.dto;


import boogi.apiserver.domain.member.domain.MemberType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DelegateMemberRequest {

    private Long memberId;
    private MemberType type;
}
