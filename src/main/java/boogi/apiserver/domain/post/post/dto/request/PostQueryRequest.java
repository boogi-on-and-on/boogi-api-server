package boogi.apiserver.domain.post.post.dto.request;

import boogi.apiserver.domain.post.post.dto.enums.PostListingOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Getter
@NoArgsConstructor
public class PostQueryRequest {

    @NotEmpty(message = "키워드를 입력해주세요")
    private String keyword;

    private PostListingOrder order = PostListingOrder.NEWER;

    public PostQueryRequest(String keyword, PostListingOrder order) {
        this.keyword = keyword;
        this.order = order;
    }
}