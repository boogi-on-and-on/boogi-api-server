package boogi.apiserver.domain.post.post.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.postmedia.domain.MediaType;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.user.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostDetail {

    @NotNull
    private Long id;

    private UserInfo user;

    private MemberInfo member;

    private CommunityInfo community;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PostMediaInfo> postMedias;

    private Long likeId;

    @NotNull
    private LocalDateTime createdAt;

    @NotEmpty
    private String content;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> hashtags;

    private int likeCount;

    private int commentCount;

    private Boolean me;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CommunityInfo {

        private Long id;
        private String name;

        private static CommunityInfo toDto(Community community) {
            return CommunityInfo.builder()
                    .id(community.getId())
                    .name(community.getCommunityName())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class MemberInfo {

        private Long id;
        private MemberType memberType;

        private static MemberInfo toDto(Member member) {
            return MemberInfo.builder()
                    .id(member.getId())
                    .memberType(member.getMemberType())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserInfo {

        private Long id;
        private String name;
        private String tagNum;
        private String profileImageUrl;

        private static UserInfo toDto(User user) {
            return UserInfo.builder()
                    .id(user.getId())
                    .name(user.getUsername())
                    .tagNum(user.getTagNumber())
                    .profileImageUrl(user.getProfileImageUrl())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class PostMediaInfo {

        private MediaType type;
        private String url;

        private static PostMediaInfo toDto(PostMedia postMedia) {
            return PostMediaInfo.builder()
                    .type(postMedia.getMediaType())
                    .url(postMedia.getMediaURL())
                    .build();
        }
    }

    public PostDetail(Post post, List<PostMedia> postMedias, Boolean me, Long likeId) {
        this.id = post.getId();
        this.user = UserInfo.toDto(post.getMember().getUser());
        this.member = MemberInfo.toDto(post.getMember());
        this.community = CommunityInfo.toDto(post.getCommunity());
        this.postMedias = postMedias.isEmpty() ? null : postMedias.stream()
                .map(pm -> PostMediaInfo.toDto(pm))
                .collect(Collectors.toList());
        this.likeId = likeId;
        this.createdAt = post.getCreatedAt();
        this.content = post.getContent();

        List<PostHashtag> postHashtags = post.getHashtags();
        this.hashtags = (postHashtags.isEmpty()) ? null : postHashtags.stream()
                .map(postHashtag -> postHashtag.getTag())
                .collect(Collectors.toList());
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.me = me;
    }
}
