package boogi.apiserver.domain.post.post.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.domain.User;
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

    private UserPostDetail user;

    private MemberPostDetail member;

    private CommunityPostDetail community;

    private Long likeId;

    @NotNull
    private LocalDateTime createdAt;

    @NotEmpty
    private String content;

    private List<String> hashtags;

    private int likeCount;

    private int commentCount;

    private Boolean me;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CommunityPostDetail {

        private Long id;
        private String name;

        private static CommunityPostDetail toDto(Community community) {
            return CommunityPostDetail.builder()
                    .id(community.getId())
                    .name(community.getCommunityName())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class MemberPostDetail {

        private Long id;
        private MemberType memberType;

        private static MemberPostDetail toDto(Member member) {
            return MemberPostDetail.builder()
                    .id(member.getId())
                    .memberType(member.getMemberType())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserPostDetail {

        private Long id;
        private String name;
        private String tagNum;
        private String profileImageUrl;

        private static UserPostDetail toDto(User user) {
            return UserPostDetail.builder()
                    .id(user.getId())
                    .name(user.getUsername())
                    .tagNum(user.getTagNumber())
                    .profileImageUrl(user.getProfileImageUrl())
                    .build();
        }
    }

    public PostDetail(Post post) {
        this.id = post.getId();
        this.user = UserPostDetail.toDto(post.getMember().getUser());
        this.member = MemberPostDetail.toDto(post.getMember());
        this.community = CommunityPostDetail.toDto(post.getCommunity());
        this.likeId = null;
        this.createdAt = post.getCreatedAt();
        this.content = post.getContent();
        this.hashtags = post.getHashtags().stream()
                .map(postHashtag -> postHashtag.getTag())
                .collect(Collectors.toList());
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.me = null;
    }

    public void setLikeId(Long likeId) {
        this.likeId = likeId;
    }

    public void setMe(Boolean me) {
        this.me = me;
    }
}
