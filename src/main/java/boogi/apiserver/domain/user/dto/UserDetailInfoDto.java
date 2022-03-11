package boogi.apiserver.domain.user.dto;

import boogi.apiserver.domain.user.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class UserDetailInfoDto {
    private String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String profileImageUrl;

    private String username;
    private String tagNum;
    private String introduce;
    private String department;

    private UserDetailInfoDto(User user) {
        this.id = user.getId().toString();
        this.profileImageUrl = user.getProfileImageUrl();
        this.username = user.getUsername();
        this.tagNum = user.getTagNumber();
        this.introduce = user.getIntroduce();
        this.department = user.getDepartment();
    }

    public static UserDetailInfoDto of(User user) {
        return new UserDetailInfoDto(user);
    }
}
