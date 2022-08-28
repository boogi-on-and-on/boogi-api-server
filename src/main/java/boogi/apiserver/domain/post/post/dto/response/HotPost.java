package boogi.apiserver.domain.post.post.dto.response;

import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.post.post.domain.Post;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
public class HotPost {
    private Long postId;
    private Integer likeCount;
    private Integer commentCount;
    private String content;
    private Long communityId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> hashtags;

    private HotPost(Post post) {
        this.postId = post.getId();
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.content = post.getContent();
        this.communityId = post.getCommunity().getId();

        List<PostHashtag> hashtags = post.getHashtags();
        if (hashtags != null && hashtags.size() != 0) {
            this.hashtags = hashtags
                    .stream()
                    .map(PostHashtag::getTag)
                    .collect(Collectors.toList());
        }
    }

    public static HotPost of(Post post) {
        return new HotPost(post);
    }
}
