package boogi.apiserver.domain.post.post.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class HotPosts {

    private static final int MAX_SIZE = 3;
    private static final String MAX_SIZE_ERROR_MESSAGE = "핫한 게시물은 " + MAX_SIZE + "개 보다 더 가져올 수 없습니다.";

    private final List<HotPost> hots;

    private HotPosts(List<HotPost> hots) {
        if (hots.size() > MAX_SIZE) {
            throw new IllegalArgumentException(MAX_SIZE_ERROR_MESSAGE);
        }
        this.hots = hots;
    }

    public static HotPosts of(List<HotPost> hots) {
        return new HotPosts(hots);
    }
}
