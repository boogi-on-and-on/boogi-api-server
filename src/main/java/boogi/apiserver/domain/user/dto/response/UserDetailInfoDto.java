package boogi.apiserver.domain.user.dto.response;

import boogi.apiserver.domain.user.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserDetailInfoDto {
    private Long id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String profileImageUrl;

    private String name;
    private String tagNum;
    private String introduce;
    private String department;

    private UserDetailInfoDto(User user) {
        this.id = user.getId();
        this.profileImageUrl = user.getProfileImageUrl();
        this.name = user.getUsername();
        this.tagNum = user.getTagNumber();
        this.introduce = user.getIntroduce();
        this.department = user.getDepartment();
    }

    public static UserDetailInfoDto of(User user) {
        return new UserDetailInfoDto(user);
    }
}
