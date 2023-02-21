package boogi.apiserver.domain.post.post.dto.dto;

import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.post.post.domain.Post;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class HotPostDto {
    private Long postId;
    private Integer likeCount;
    private Integer commentCount;
    private String content;
    private Long communityId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> hashtags;

    @Builder(access = AccessLevel.PRIVATE)
    public HotPostDto(Long postId, Integer likeCount, Integer commentCount,
                      String content, Long communityId, List<String> hashtags) {
        this.postId = postId;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.content = content;
        this.communityId = communityId;
        this.hashtags = hashtags;
    }

    public static HotPostDto from(Post post) {
        List<PostHashtag> postHashtags = post.getHashtags();
        List<String> hashtags = (postHashtags == null || postHashtags.size() == 0) ? null : postHashtags
                .stream()
                .map(PostHashtag::getTag)
                .collect(Collectors.toList());

        return HotPostDto.builder()
                .postId(post.getId())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .content(post.getContent())
                .communityId(post.getCommunityId())
                .hashtags(hashtags)
                .build();
    }

    // todo: Projection 사용으로 변경한 후 mapOf 제거
    public static List<HotPostDto> mapOf(List<Post> posts) {
        return posts.stream()
                .map(HotPostDto::from)
                .collect(Collectors.toList());
    }
}
