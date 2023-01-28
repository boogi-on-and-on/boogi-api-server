package boogi.apiserver.domain.post.post.dto.response;

import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.global.dto.PaginationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@AllArgsConstructor
@Data
public class UserPostPage {
    private List<UserPostsDto> posts;
    PaginationDto pageInfo;

    private UserPostPage(Slice<Post> page) {
        this.posts = page.getContent().stream()
                .map(UserPostsDto::of)
                .collect(Collectors.toList());
        this.pageInfo = PaginationDto.of(page);
    }

    public static UserPostPage from(Slice<Post> page) {
        return new UserPostPage(page);
    }

}
