package boogi.apiserver.domain.community.community.dto.dto;

import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class UserJoinRequestInfoDto {

    private final UserBasicProfileDto user;
    private final Long id;

    @Builder(access = AccessLevel.PRIVATE)
    public UserJoinRequestInfoDto(UserBasicProfileDto user, Long id) {
        this.user = user;
        this.id = id;
    }

    public static UserJoinRequestInfoDto of(JoinRequest joinRequest) {
        return UserJoinRequestInfoDto.builder()
                .user(UserBasicProfileDto.from(joinRequest.getUser()))
                .id(joinRequest.getId())
                .build();
    }

    public static List<UserJoinRequestInfoDto> listOf(List<JoinRequest> joinRequests) {
        return joinRequests.stream()
                .map(UserJoinRequestInfoDto::of)
                .collect(Collectors.toList());
    }
}
