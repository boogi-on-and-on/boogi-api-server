package boogi.apiserver.domain.post.post.dto.response;


import boogi.apiserver.domain.post.post.dto.dto.SearchPostDto;
import boogi.apiserver.global.dto.PaginationDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchPostsResponse {

    private List<SearchPostDto> posts;

    private PaginationDto pageInfo;

    public SearchPostsResponse(List<SearchPostDto> posts, PaginationDto pageInfo) {
        this.posts = posts;
        this.pageInfo = pageInfo;
    }

    public static SearchPostsResponse from(Slice<SearchPostDto> page) {
        PaginationDto pageInfo = PaginationDto.of(page);
        List<SearchPostDto> posts = page.getContent();
        return new SearchPostsResponse(posts, pageInfo);
    }
}
