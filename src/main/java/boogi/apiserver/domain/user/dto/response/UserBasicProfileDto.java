package boogi.apiserver.domain.user.dto.response;

import boogi.apiserver.domain.user.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserBasicProfileDto {
    private Long id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String profileImageUrl;
    private String tagNum;
    private String name;

    @QueryProjection
    public UserBasicProfileDto(User user) {
        this.id = user.getId();
        this.profileImageUrl = user.getProfileImageUrl();
        this.tagNum = user.getTagNumber();
        this.name = user.getUsername();
    }

    public static UserBasicProfileDto of(User user) {
        return new UserBasicProfileDto(user);
    }
}
