package boogi.apiserver.domain.post.post.dto;

import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.global.dto.PagnationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@AllArgsConstructor
@Data
public class UserPostPage {
    private List<UserPostsDto> posts;
    PagnationDto pageInfo;

    private UserPostPage(Page<Post> page) {
        this.posts = page.getContent().stream()
                .map(UserPostsDto::of)
                .collect(Collectors.toList());
        this.pageInfo = PagnationDto.of(page);
    }

    public static UserPostPage of(Page<Post> page) {
        return new UserPostPage(page);
    }

}
