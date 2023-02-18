package boogi.apiserver.domain.user.dto.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class UserJoinedCommunityDto {
    private String name;
    private Long id;

    public static UserJoinedCommunityDto of(Community community) {
        return UserJoinedCommunityDto.builder()
                .id(community.getId())
                .name(community.getCommunityName())
                .build();
    }
}
