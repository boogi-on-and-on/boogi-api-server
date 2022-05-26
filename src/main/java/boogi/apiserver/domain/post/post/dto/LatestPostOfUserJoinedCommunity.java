package boogi.apiserver.domain.post.post.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
public class LatestPostOfUserJoinedCommunity {
    private String name;
    private Long id;
    private PostDto post;

    @Builder
    @AllArgsConstructor
    @Data
    public static class PostDto {
        private Long id;
        private String createdAt;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<String> hashtags;

        private String content;
        private Integer likeCount;
        private Integer commentCount;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String postMediaUrl;

        private PostDto(Post post) {
            this.id = post.getId();
            this.createdAt = post.getCreatedAt().toString();
            if (post.getHashtags().size() > 0) {
                this.hashtags = post.getHashtags().stream().map(PostHashtag::getTag).collect(Collectors.toList());
            }

            List<PostMedia> postMedias = post.getPostMedias();
            if (postMedias.size() > 0) {
                this.postMediaUrl = postMedias.get(0).getMediaURL();
            }

            this.content = post.getContent();
            this.likeCount = post.getLikeCount();
            this.commentCount = post.getCommentCount();
        }
    }

    private LatestPostOfUserJoinedCommunity(Post post) {
        Community community = post.getCommunity();
        this.name = community.getCommunityName();
        this.id = community.getId();
        this.post = new PostDto(post);
    }

    public static LatestPostOfUserJoinedCommunity of(Post post) {
        return new LatestPostOfUserJoinedCommunity(post);
    }
}
