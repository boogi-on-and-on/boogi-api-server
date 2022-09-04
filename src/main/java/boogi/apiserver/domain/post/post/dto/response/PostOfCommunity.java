package boogi.apiserver.domain.post.post.dto.response;

import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.dto.response.PostMediaMetadataDto;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
    private LocalDateTime createdAt;
    private String content;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> hashtags;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PostMediaMetadataDto> postMedias;

    private Integer likeCount;
    private Integer commentCount;
    private Boolean me;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long likeId;

    public PostOfCommunity(Post post, Long userId, Member sessionMember) {
        Member member = post.getMember();

        this.id = post.getId();
        this.user = new UserDto(member.getUser());
        this.createdAt = post.getCreatedAt();
        this.content = post.getContent();

        List<PostHashtag> hashtags = post.getHashtags();
        if (Objects.nonNull(hashtags) && hashtags.size() > 0) {
            this.hashtags = hashtags.stream()
                    .map(PostHashtag::getTag)
                    .collect(Collectors.toList());
        }

        List<PostMedia> postMedias = post.getPostMedias();
        if (postMedias != null && postMedias.size() > 0) {
            this.postMedias = postMedias.stream()
                    .map(PostMediaMetadataDto::of)
                    .collect(Collectors.toList());
        }

        List<Like> likes = post.getLikes();
        if (Objects.nonNull(sessionMember) && likes != null && likes.size() > 0) {
            likes.stream()
                    .filter(l -> l.getMember().getId().equals(sessionMember.getId()))
                    .findFirst()
                    .ifPresent(l -> this.likeId = l.getId());
        }

        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.me = member.getUser().getId().equals(userId);
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
