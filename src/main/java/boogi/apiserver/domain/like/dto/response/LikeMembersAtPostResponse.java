package boogi.apiserver.domain.like.dto.response;


import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.global.dto.PaginationDto;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class LikeMembersAtPostResponse {

    private List<UserBasicProfileDto> members;

    private PaginationDto pageInfo;

    public LikeMembersAtPostResponse(List<UserBasicProfileDto> members, PaginationDto pageInfo) {
        this.members = members;
        this.pageInfo = pageInfo;
    }

    public static LikeMembersAtPostResponse of(List<User> users, Slice page) {
        List<UserBasicProfileDto> members = users.stream()
                .map(UserBasicProfileDto::of)
                .collect(Collectors.toList());
        PaginationDto pageInfo = PaginationDto.of(page);

        return new LikeMembersAtPostResponse(members, pageInfo);
    }
}
