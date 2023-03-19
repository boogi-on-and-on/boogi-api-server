package boogi.apiserver.domain.comment.dto.response;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.dto.UserCommentDto;
import boogi.apiserver.global.dto.PaginationDto;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
public class UserCommentPageResponse {

    private List<UserCommentDto> comments;
    private PaginationDto pageInfo;

    public UserCommentPageResponse(final List<UserCommentDto> comments, final PaginationDto pageInfo) {
        this.comments = comments;
        this.pageInfo = pageInfo;
    }

    public static UserCommentPageResponse of(Slice<Comment> page) {
        final List<UserCommentDto> comments = UserCommentDto.listOf(page.getContent());

        return new UserCommentPageResponse(comments, PaginationDto.of(page));
    }
}
