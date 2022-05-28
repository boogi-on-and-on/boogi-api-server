package boogi.apiserver.domain.user.dto;

import boogi.apiserver.domain.user.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserDetailInfoResponse {
    private Long id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String profileImageUrl;

    private String name;
    private String tagNum;
    private String introduce;
    private String department;

    private UserDetailInfoResponse(User user) {
        this.id = user.getId();
        this.profileImageUrl = user.getProfileImageUrl();
        this.name = user.getUsername();
        this.tagNum = user.getTagNumber();
        this.introduce = user.getIntroduce();
        this.department = user.getDepartment();
    }

    public static UserDetailInfoResponse of(User user) {
        return new UserDetailInfoResponse(user);
    }
}
