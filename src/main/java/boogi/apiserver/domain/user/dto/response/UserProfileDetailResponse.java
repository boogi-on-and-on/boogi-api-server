package boogi.apiserver.domain.user.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserProfileDetailResponse {

    private final UserDetailInfoDto user;
    private final Boolean me;

    @Builder(access = AccessLevel.PRIVATE)
    private UserProfileDetailResponse(final UserDetailInfoDto user, final Boolean me) {
        this.user = user;
        this.me = me;
    }

    public static UserProfileDetailResponse of(final UserDetailInfoDto user, final Long sessionUserId) {
        return UserProfileDetailResponse.builder()
                .user(user)
                .me(sessionUserId.equals(user.getId()))
                .build();
    }
}
