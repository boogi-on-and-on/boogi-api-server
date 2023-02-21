package boogi.apiserver.domain.community.community.dto.response;

import boogi.apiserver.domain.community.community.dto.dto.UserJoinRequestInfoDto;
import lombok.Getter;

import java.util.List;

@Getter
public class UserJoinRequestsResponse {

    private final List<UserJoinRequestInfoDto> requests;

    public UserJoinRequestsResponse(List<UserJoinRequestInfoDto> requests) {
        this.requests = requests;
    }

    public static UserJoinRequestsResponse from(List<UserJoinRequestInfoDto> requests) {
        return new UserJoinRequestsResponse(requests);
    }
}
