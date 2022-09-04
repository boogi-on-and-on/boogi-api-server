package boogi.apiserver.domain.post.post.dto.response;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.dto.response.PostMediaMetadataDto;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@AllArgsConstructor
@Data
public class UserPostsDto {
    private String content;
    private CommunityDto community;

    @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
    private LocalDateTime createdAt;
    private Long id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> hashtags;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PostMediaMetadataDto> postMedias;

    @Builder
    @AllArgsConstructor
    @Data
    public static class CommunityDto {
        private Long id;
        private String name;

        private CommunityDto(Community community) {
            this.id = community.getId();
            this.name = community.getCommunityName();
        }
    }

    private UserPostsDto(Post post) {
        this.content = post.getContent();
        this.id = post.getId();
        this.community = new CommunityDto(post.getCommunity());
        this.createdAt = post.getCreatedAt();
        List<PostHashtag> hashtags = post.getHashtags();
        if (hashtags.size() > 0) {
            this.hashtags = hashtags.stream().map(PostHashtag::getTag).collect(Collectors.toList());
        }

        List<PostMedia> postMedias = post.getPostMedias();
        if (postMedias != null && postMedias.size() > 0) {
            this.postMedias = postMedias.stream()
                    .map(PostMediaMetadataDto::of)
                    .collect(Collectors.toList());
        }
    }

    public static UserPostsDto of(Post post) {
        return new UserPostsDto(post);
    }
}
