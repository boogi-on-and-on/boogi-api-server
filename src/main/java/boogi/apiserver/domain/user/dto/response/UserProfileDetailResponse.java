package boogi.apiserver.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserProfileDetailResponse {

    private final UserDetailInfoDto user;
    private final Boolean me;

    private UserProfileDetailResponse(final UserDetailInfoDto user, final Boolean me) {
        this.user = user;
        this.me = me;
    }

    public static UserProfileDetailResponse of(final UserDetailInfoDto user, final Long sessionUserId) {
        boolean me = sessionUserId.equals(user.getId());
        return new UserProfileDetailResponse(user, me);
    }
}
