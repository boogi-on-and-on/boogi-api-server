package boogi.apiserver.domain.community.community.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Builder
public class JoinedCommunities {

    List<CommunityInfo> communities;

    public static JoinedCommunities of(Map<Long, Community> joinedCommunityMap, Map<Long, Post> latestPostMap, Map<Long, String> postMediaUrlMap) {
        List<CommunityInfo> communitiesWithPosts = latestPostMap.keySet().stream()
                .map(ci -> {
                    Community joinedCommunity = joinedCommunityMap.get(ci);
                    joinedCommunityMap.remove(ci);
                    Post latestPost = latestPostMap.get(ci);
                    return CommunityInfo.toDto(joinedCommunity, latestPost, postMediaUrlMap.get(latestPost.getId()));
                }).collect(Collectors.toList());

        List<CommunityInfo> communitiesWithoutPosts = joinedCommunityMap.keySet().stream()
                .map(ci ->
                        CommunityInfo.toDto(joinedCommunityMap.get(ci), null, null)
                ).collect(Collectors.toList());

        List<CommunityInfo> communities = (communitiesWithPosts.isEmpty() && communitiesWithoutPosts.isEmpty()) ? new ArrayList<>() :
                Stream.concat(communitiesWithPosts.stream(), communitiesWithoutPosts.stream())
                        .collect(Collectors.toList());

        return JoinedCommunities.builder()
                .communities(communities)
                .build();
    }

    @Getter
    @Builder
    static class CommunityInfo {
        private Long id;
        private String name;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private PostInfo post;

        public static CommunityInfo toDto(Community community, Post post, String postMediaUrl) {
            return CommunityInfo.builder()
                    .id(community.getId())
                    .name(community.getCommunityName())
                    .post((post == null) ? null : PostInfo.toDto(post, postMediaUrl))
                    .build();
        }
    }


    @Getter
    @Builder
    static class PostInfo {
        private Long id;

        @JsonFormat(pattern = TimePattern.BASIC_FORMAT)
        private LocalDateTime createdAt;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<String> hashtags;

        private String content;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String postMediaUrl;

        private Integer likeCount;
        private Integer commentCount;

        private static PostInfo toDto(Post post, String postMediaUrl) {
            List<PostHashtag> postHashtags = post.getHashtags();

            return PostInfo.builder()
                    .id(post.getId())
                    .createdAt(post.getCreatedAt())
                    .hashtags((postHashtags.isEmpty()) ? null : postHashtags.stream()
                            .map(postHashtag -> postHashtag.getTag())
                            .collect(Collectors.toList()))
                    .content(post.getContent())
                    .postMediaUrl(postMediaUrl)
                    .likeCount(post.getLikeCount())
                    .commentCount(post.getCommentCount())
                    .build();
        }
    }
}
