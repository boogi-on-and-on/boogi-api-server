package boogi.apiserver.domain.community.community.dto.response;

import boogi.apiserver.domain.community.community.dto.dto.UserJoinRequestInfoDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserJoinRequestsResponse {

    private List<UserJoinRequestInfoDto> requests;

    public UserJoinRequestsResponse(List<UserJoinRequestInfoDto> requests) {
        this.requests = requests;
    }

    public static UserJoinRequestsResponse from(List<UserJoinRequestInfoDto> requests) {
        return new UserJoinRequestsResponse(requests);
    }
}
