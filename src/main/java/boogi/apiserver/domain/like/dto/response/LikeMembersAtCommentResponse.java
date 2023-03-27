package boogi.apiserver.domain.like.dto.response;

import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.global.dto.PaginationDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
public class LikeMembersAtCommentResponse {

    private List<UserBasicProfileDto> members;

    private PaginationDto pageInfo;

    @Builder(access = AccessLevel.PRIVATE)
    public LikeMembersAtCommentResponse(List<UserBasicProfileDto> users, PaginationDto pageInfo) {
        this.members = users;
        this.pageInfo = pageInfo;
    }

    public static LikeMembersAtCommentResponse of(List<User> users, Slice page) {
        final LikeMembersAtCommentResponseBuilder builder = LikeMembersAtCommentResponse.builder()
                .pageInfo(PaginationDto.of(page));

        if (users != null && users.size() > 0) {
            builder.users(UserBasicProfileDto.listFrom(users));
        }
        return builder.build();
    }
}
