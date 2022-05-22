package boogi.apiserver.domain.comment.dto;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.global.dto.PagnationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
public class UserCommentPage {

    private List<UserCommentDto> comments;
    PagnationDto pageInfo;

    private UserCommentPage(Page<Comment> page) {
        this.comments = page.getContent().stream()
                .map(UserCommentDto::of)
                .collect(Collectors.toList());

        pageInfo = PagnationDto.of(page);
    }

    public static UserCommentPage of(Page<Comment> page) {
        return new UserCommentPage(page);
    }

}
