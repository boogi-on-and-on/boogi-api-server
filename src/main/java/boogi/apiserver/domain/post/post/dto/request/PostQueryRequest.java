package boogi.apiserver.domain.post.post.dto.request;

import boogi.apiserver.domain.post.post.dto.enums.PostListingOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostQueryRequest {

    @NotEmpty(message = "키워드를 입력해주세요")
    private String keyword;

    private PostListingOrder order = PostListingOrder.NEWER;
}