package boogi.apiserver.domain.post.post.dto;

import boogi.apiserver.domain.post.post.domain.Post;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class UserPostPage {
    private List<UserPostsDto> posts;
    private int nextPage;
    private int totalCount;
    private boolean hasNext;

    private UserPostPage(Page<Post> page) {
        this.posts = page.getContent().stream().map(UserPostsDto::of).collect(Collectors.toList());
        this.nextPage = page.getNumber() + 1;
        this.totalCount = (int) page.getTotalElements();
        this.hasNext = page.hasNext();
    }

    public static UserPostPage of(Page<Post> page) {
        return new UserPostPage(page);
    }

}
