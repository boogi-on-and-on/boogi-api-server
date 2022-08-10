package boogi.apiserver.domain.like.dto;

import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.dto.PagnationDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LikeMembersAtComment {

    private List<UserInfo> members = new ArrayList<>();

    private PagnationDto pageInfo;

    @Getter
    @Builder
    public static class UserInfo {
        private Long id;
        private String name;
        private String tagNum;
        private String profileImageUrl;

        public static UserInfo toDto(User user) {
            return UserInfo.builder()
                    .id(user.getId())
                    .name(user.getUsername())
                    .tagNum(user.getTagNumber())
                    .profileImageUrl(user.getProfileImageUrl())
                    .build();
        }
    }

    public LikeMembersAtComment(List<User> users, Page page) {
        if (users != null && users.size() > 0) {
            this.members = users.stream()
                    .map(user -> UserInfo.toDto(user))
                    .collect(Collectors.toList());
        }
        this.pageInfo = PagnationDto.of(page);
    }
}
