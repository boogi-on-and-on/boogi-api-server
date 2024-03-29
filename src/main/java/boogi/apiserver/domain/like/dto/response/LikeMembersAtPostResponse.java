package boogi.apiserver.domain.like.dto.response;


import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.global.dto.PaginationDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeMembersAtPostResponse {

    private List<UserBasicProfileDto> members;

    private PaginationDto pageInfo;

    public LikeMembersAtPostResponse(List<UserBasicProfileDto> members, PaginationDto pageInfo) {
        this.members = members;
        this.pageInfo = pageInfo;
    }

    public static LikeMembersAtPostResponse of(List<User> users, Slice page) {
        List<UserBasicProfileDto> members = UserBasicProfileDto.listFrom(users);
        PaginationDto pageInfo = PaginationDto.of(page);

        return new LikeMembersAtPostResponse(members, pageInfo);
    }
}
