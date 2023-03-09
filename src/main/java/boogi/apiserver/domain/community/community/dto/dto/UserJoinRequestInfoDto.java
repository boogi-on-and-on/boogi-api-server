package boogi.apiserver.domain.community.community.dto.dto;

import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserJoinRequestInfoDto {

    private final UserBasicProfileDto user;
    private final Long id;

    @Builder(access = AccessLevel.PRIVATE)
    private UserJoinRequestInfoDto(UserBasicProfileDto user, Long id) {
        this.user = user;
        this.id = id;
    }

    public static UserJoinRequestInfoDto of(User user, Long id) {
        return UserJoinRequestInfoDto.builder()
                .user(UserBasicProfileDto.from(user))
                .id(id)
                .build();
    }
}
