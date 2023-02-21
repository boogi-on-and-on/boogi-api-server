package boogi.apiserver.domain.post.post.dto.response;

import boogi.apiserver.domain.post.post.dto.dto.HotPostDto;
import lombok.Getter;

import java.util.List;

@Getter
public class HotPostsResponse {
    private final List<HotPostDto> hots;

    public HotPostsResponse(List<HotPostDto> hots) {
        this.hots = hots;
    }

    public static HotPostsResponse from(List<HotPostDto> hots) {
        return new HotPostsResponse(hots);
    }
}
