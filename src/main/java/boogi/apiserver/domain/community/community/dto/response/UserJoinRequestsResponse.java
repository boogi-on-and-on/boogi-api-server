package boogi.apiserver.domain.community.community.dto.response;

import boogi.apiserver.domain.community.community.dto.dto.UserJoinRequestInfoDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class UserJoinRequestsResponse {

    private final List<UserJoinRequestInfoDto> requests;

    @Builder(access = AccessLevel.PRIVATE)
    private UserJoinRequestsResponse(List<UserJoinRequestInfoDto> requests) {
        this.requests = requests;
    }

    public static UserJoinRequestsResponse of(List<UserJoinRequestInfoDto> requests) {
        return UserJoinRequestsResponse.builder()
                .requests(requests)
                .build();
    }
}
