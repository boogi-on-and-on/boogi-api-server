package boogi.apiserver.domain.user.dto.response;

import boogi.apiserver.domain.user.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserDetailInfoDto {
    private Long id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String profileImageUrl;

    private String name;
    private String tagNum;
    private String introduce;
    private String department;

    @Builder(access = AccessLevel.PRIVATE)
    public UserDetailInfoDto(Long id, String profileImageUrl, String name, String tagNum,
                             String introduce, String department) {
        this.id = id;
        this.profileImageUrl = profileImageUrl;
        this.name = name;
        this.tagNum = tagNum;
        this.introduce = introduce;
        this.department = department;
    }

    public static UserDetailInfoDto of(User user) {
        return UserDetailInfoDto.builder()
                .id(user.getId())
                .profileImageUrl(user.getProfileImageUrl())
                .name(user.getUsername())
                .tagNum(user.getTagNumber())
                .introduce(user.getIntroduce())
                .department(user.getDepartment())
                .build();
    }
}
