package boogi.apiserver.domain.community.community.dto.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JoinedCommunitiesDto {

    private List<CommunityInfo> communities;

    public JoinedCommunitiesDto(List<CommunityInfo> communities) {
        this.communities = communities;
    }

    public static JoinedCommunitiesDto of(Map<Long, Community> joinedCommunityMap,
                                          List<Post> latestPosts,
                                          List<PostMedia> postMedias) {

        Map<Long, Post> latestPostMap = toLatestPostMap(latestPosts);
        Map<Long, String> postMediaUrlMap = toPostMediaUrlMap(postMedias);

        List<CommunityInfo> communitiesWithPosts = latestPostMap.keySet().stream()
                .map(ci -> getInfoWithPost(joinedCommunityMap, latestPostMap, postMediaUrlMap, ci))
                .collect(Collectors.toList());

        List<CommunityInfo> communitiesWithoutPosts = joinedCommunityMap.keySet().stream()
                .filter(ci -> !latestPostMap.containsKey(ci))
                .map(ci -> getInfoWithoutPost(joinedCommunityMap, ci))
                .collect(Collectors.toList());

        List<CommunityInfo> communities = Stream.concat(communitiesWithPosts.stream(), communitiesWithoutPosts.stream())
                .collect(Collectors.toList());

        return new JoinedCommunitiesDto(communities);
    }

    private static HashMap<Long, String> toPostMediaUrlMap(List<PostMedia> postMedias) {
        return postMedias.stream()
                .collect(Collectors.toMap(
                        pm1 -> pm1.getPost().getId(),
                        pm2 -> pm2.getMediaURL(),
                        (o, n) -> n,
                        HashMap::new
                ));
    }

    private static LinkedHashMap<Long, Post> toLatestPostMap(List<Post> latestPosts) {
        return latestPosts.stream()
                .collect(Collectors.toMap(
                        lp1 -> lp1.getCommunity().getId(),
                        lp2 -> lp2,
                        (o, n) -> n,
                        LinkedHashMap::new
                ));
    }

    private static CommunityInfo getInfoWithoutPost(Map<Long, Community> joinedCommunityMap, Long ci) {
        return CommunityInfo.of(joinedCommunityMap.get(ci), null, null);
    }

    private static CommunityInfo getInfoWithPost(Map<Long, Community> joinedCommunityMap,
                                                 Map<Long, Post> latestPostMap,
                                                 Map<Long, String> postMediaUrlMap,
                                                 Long ci) {
        Community joinedCommunity = joinedCommunityMap.get(ci);
        Post latestPost = latestPostMap.get(ci);
        return CommunityInfo.of(joinedCommunity, latestPost, postMediaUrlMap.get(latestPost.getId()));
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CommunityInfo {
        private Long id;
        private String name;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private PostInfo post;

        @Builder(access = AccessLevel.PRIVATE)
        public CommunityInfo(Long id, String name, PostInfo post) {
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
    public static class PostInfo {
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
        public PostInfo(Long id, LocalDateTime createdAt, List<String> hashtags,
                        String content, String postMediaUrl, Integer likeCount,
                        Integer commentCount) {
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
