package boogi.apiserver.domain.comment.dto.response;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.global.dto.PaginationDto;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class UserCommentPageResponse {

    private List<UserCommentDto> comments;
    private PaginationDto pageInfo;

    public UserCommentPageResponse(final List<UserCommentDto> comments, final PaginationDto pageInfo) {
        this.comments = comments;
        this.pageInfo = pageInfo;
    }

    public static UserCommentPageResponse of(Slice<Comment> page) {
        final List<UserCommentDto> comments = page.getContent().stream()
                .map(UserCommentDto::of)
                .collect(Collectors.toList());

        return new UserCommentPageResponse(comments, PaginationDto.of(page));

    }

}
