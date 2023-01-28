package boogi.apiserver.domain.post.post.dto.request;

import boogi.apiserver.domain.post.post.dto.enums.PostListingOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@Builder
public class PostQueryRequest {

    @NotEmpty(message = "키워드를 입력해주세요")
    private String keyword;

    @Builder.Default
    private PostListingOrder order = PostListingOrder.NEWER;
}