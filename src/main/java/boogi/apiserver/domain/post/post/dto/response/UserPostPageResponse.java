package boogi.apiserver.domain.post.post.dto.response;

import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.dto.UserPostDto;
import boogi.apiserver.global.dto.PaginationDto;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class UserPostPageResponse {
    private List<UserPostDto> posts;
    private PaginationDto pageInfo;

    public UserPostPageResponse(List<UserPostDto> posts, PaginationDto pageInfo) {
        this.posts = posts;
        this.pageInfo = pageInfo;
    }

    public static UserPostPageResponse from(Slice<Post> page) {
        List<UserPostDto> posts = page.getContent().stream()
                .map(UserPostDto::from)
                .collect(Collectors.toList());
        PaginationDto pageInfo = PaginationDto.of(page);

        return new UserPostPageResponse(posts, pageInfo);
    }
}
