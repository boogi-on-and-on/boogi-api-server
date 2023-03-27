package boogi.apiserver.domain.community.community.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@NoArgsConstructor
@Getter
public class JoinRequestIdsRequest {

    @NotEmpty
    private List<Long> requestIds;

    public JoinRequestIdsRequest(List<Long> requestIds) {
        this.requestIds = requestIds;
    }
}
