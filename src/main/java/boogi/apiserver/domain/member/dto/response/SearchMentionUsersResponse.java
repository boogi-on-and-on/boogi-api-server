package boogi.apiserver.domain.member.dto.response;

import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.global.dto.PaginationDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
public class SearchMentionUsersResponse {

    private final List<UserBasicProfileDto> users;
    private final PaginationDto pageInfo;


    @Builder(access = AccessLevel.PRIVATE)
    public SearchMentionUsersResponse(List<UserBasicProfileDto> users, PaginationDto pageInfo) {
        this.users = users;
        this.pageInfo = pageInfo;
    }

    public static SearchMentionUsersResponse from(Slice<UserBasicProfileDto> slice) {
        return SearchMentionUsersResponse.builder()
                .users(slice.getContent())
                .pageInfo(PaginationDto.of(slice))
                .build();
    }
}
