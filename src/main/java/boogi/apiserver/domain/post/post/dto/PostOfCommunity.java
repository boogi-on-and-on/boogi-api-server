package boogi.apiserver.domain.post.post.dto;

import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PostOfCommunity {

    private Long id;
    private UserDto user;
    private String createdAt;
    private String content;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> hashtags;

    private Integer likeCount;
    private Integer commentCount;
    private Boolean me;

    public PostOfCommunity(Post post, Long userId) {
        this.id = post.getId();
        this.user = new UserDto(post.getMember().getUser());
        this.createdAt = post.getCreatedAt().toString();
        this.content = post.getContent();

        List<PostHashtag> hashtags = post.getHashtags();
        if (Objects.nonNull(hashtags) && hashtags.size() > 0) {
            this.hashtags = hashtags.stream()
                    .map(PostHashtag::getTag)
                    .collect(Collectors.toList());
        }

        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.me = post.getMember().getUser().getId().equals(userId);
    }



    @Data
    @Builder
    @AllArgsConstructor
    static class UserDto {
        private Long id;
        private String name;
        private String tagNum;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String profileImageUrl;

        public UserDto(User user) {
            this.id = user.getId();
            this.name = user.getUsername();
            this.tagNum = user.getTagNumber();
            this.profileImageUrl = user.getProfileImageUrl();
        }
    }
}
