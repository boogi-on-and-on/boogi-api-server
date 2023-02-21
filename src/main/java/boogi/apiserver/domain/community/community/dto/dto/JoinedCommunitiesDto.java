package boogi.apiserver.domain.community.community.dto.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class JoinedCommunitiesDto {

    private List<CommunityInfo> communities;

    public JoinedCommunitiesDto(final List<CommunityInfo> communities) {
        this.communities = communities;
    }

    public static JoinedCommunitiesDto of(Map<Long, Community> joinedCommunityMap,
                                          Map<Long, Post> latestPostMap,
                                          Map<Long, String> postMediaUrlMap) {
        List<CommunityInfo> communitiesWithPosts = latestPostMap.keySet().stream()
                .map(ci -> {
                    Community joinedCommunity = joinedCommunityMap.get(ci);
                    joinedCommunityMap.remove(ci);
                    Post latestPost = latestPostMap.get(ci);
                    return CommunityInfo.of(joinedCommunity, latestPost, postMediaUrlMap.get(latestPost.getId()));
                }).collect(Collectors.toList());

        List<CommunityInfo> communitiesWithoutPosts = joinedCommunityMap.keySet().stream()
                .map(ci ->
                        CommunityInfo.of(joinedCommunityMap.get(ci), null, null)
                ).collect(Collectors.toList());

        List<CommunityInfo> communities = (communitiesWithPosts.isEmpty() && communitiesWithoutPosts.isEmpty()) ? new ArrayList<>() :
                Stream.concat(communitiesWithPosts.stream(), communitiesWithoutPosts.stream())
                        .collect(Collectors.toList());

        return new JoinedCommunitiesDto(communities);
    }

    @Getter
    static class CommunityInfo {
        private Long id;
        private String name;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private PostInfo post;

        @Builder(access = AccessLevel.PRIVATE)
        public CommunityInfo(final Long id, final String name, final PostInfo post) {
            this.id = id;
            this.name = name;
            this.post = post;
        }

        public static CommunityInfo of(Community community, Post post, String postMediaUrl) {
            return CommunityInfo.builder()
                    .id(community.getId())
                    .name(community.getCommunityName())
                    .post(PostInfo.of(post, postMediaUrl))
                    .build();
        }
    }


    @Getter
    static class PostInfo {
        private Long id;

        @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
        private LocalDateTime createdAt;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<String> hashtags;

        private String content;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String postMediaUrl;

        private Integer likeCount;
        private Integer commentCount;

        @Builder(access = AccessLevel.PRIVATE)
        public PostInfo(final Long id, final LocalDateTime createdAt, final List<String> hashtags,
                        final String content, final String postMediaUrl, final Integer likeCount,
                        final Integer commentCount) {
            this.id = id;
            this.createdAt = createdAt;
            this.hashtags = hashtags;
            this.content = content;
            this.postMediaUrl = postMediaUrl;
            this.likeCount = likeCount;
            this.commentCount = commentCount;
        }

        private static PostInfo of(Post post, String postMediaUrl) {
            if (post == null) {
                return null;
            }
            List<PostHashtag> postHashtags = post.getHashtags();
            List<String> hashtags = postHashtags.isEmpty() ? null :
                    postHashtags.stream()
                            .map(PostHashtag::getTag)
                            .collect(Collectors.toList());

            return PostInfo.builder()
                    .id(post.getId())
                    .createdAt(post.getCreatedAt())
                    .hashtags(hashtags)
                    .content(post.getContent())
                    .postMediaUrl(postMediaUrl)
                    .likeCount(post.getLikeCount())
                    .commentCount(post.getCommentCount())
                    .build();
        }
    }
}
