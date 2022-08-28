package boogi.apiserver.domain.comment.dto.response;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.global.dto.PaginationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
public class UserCommentPage {

    private List<UserCommentDto> comments;
    PaginationDto pageInfo;

    private UserCommentPage(Slice<Comment> page) {
        this.comments = page.getContent().stream()
                .map(UserCommentDto::of)
                .collect(Collectors.toList());

        pageInfo = PaginationDto.of(page);
    }

    public static UserCommentPage of(Slice<Comment> page) {
        return new UserCommentPage(page);
    }

}
