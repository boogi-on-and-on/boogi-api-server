package boogi.apiserver.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockMessageUsersRequest {

    @NotEmpty(message = "메시지 차단할 유저를 1명이상 선택해주세요")
    private List<Long> blockUserIds;
}
