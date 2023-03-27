package boogi.apiserver.domain.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;


@Getter
@NoArgsConstructor
public class BlockMessageUsersRequest {
    @NotEmpty(message = "쪽지 차단할 유저를 1명이상 선택해주세요")
    private List<Long> blockUserIds;

    public BlockMessageUsersRequest(List<Long> blockUserIds) {
        this.blockUserIds = blockUserIds;
    }
}
