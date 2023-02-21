package boogi.apiserver.domain.user.dto.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import lombok.Getter;

@Getter
public class UserJoinedCommunityDto {
    private Long id;
    private String name;

    public UserJoinedCommunityDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static UserJoinedCommunityDto from(Community community) {
        return new UserJoinedCommunityDto(community.getId(), community.getCommunityName());
    }
}
