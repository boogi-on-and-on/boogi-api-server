package boogi.apiserver.domain.comment.dto;

import boogi.apiserver.domain.comment.domain.Comment;
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
    private int nextPage;
    private int totalCount;
    private boolean hasNext;

    private UserCommentPage(Page<Comment> page) {
        this.comments = page.getContent().stream()
                .map(UserCommentDto::of)
                .collect(Collectors.toList());
        this.nextPage = page.getNumber();
        this.totalCount = (int) page.getTotalElements();
        this.hasNext = page.hasNext();
    }

    public static UserCommentPage of(Page<Comment> page) {
        return new UserCommentPage(page);
    }

}
