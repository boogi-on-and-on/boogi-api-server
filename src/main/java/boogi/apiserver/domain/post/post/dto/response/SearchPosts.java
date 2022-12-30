package boogi.apiserver.domain.post.post.dto.response;


import boogi.apiserver.global.dto.PaginationDto;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
public class SearchPosts {

    private List<SearchPostDto> posts;

    private PaginationDto pageInfo;

    private SearchPosts(List<SearchPostDto> posts, PaginationDto pageInfo) {
        this.posts = posts;
        this.pageInfo = pageInfo;
    }

    public static SearchPosts from(Slice<SearchPostDto> page) {
        PaginationDto pageInfo = PaginationDto.of(page);
        List<SearchPostDto> posts = page.getContent();
        return new SearchPosts(posts, pageInfo);
    }
}
