package boogi.apiserver.domain.post.post.dto.response;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.postmedia.domain.MediaType;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostDetailResponse {

    private Long id;

    private UserBasicProfileDto user;

    private MemberInfo member;

    private CommunityInfo community;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PostMediaInfo> postMedias;

    private Long likeId;

    @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
    private LocalDateTime createdAt;

    private String content;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> hashtags;

    private int likeCount;

    private int commentCount;

    private Boolean me;

    @Builder(access = AccessLevel.PRIVATE)
    public PostDetailResponse(Long id, UserBasicProfileDto user, MemberInfo member,
                              CommunityInfo community, List<PostMediaInfo> postMedias,
                              Long likeId, LocalDateTime createdAt, String content,
                              List<String> hashtags, int likeCount, int commentCount, Boolean me) {
        this.id = id;
        this.user = user;
        this.member = member;
        this.community = community;
        this.postMedias = postMedias;
        this.likeId = likeId;
        this.createdAt = createdAt;
        this.content = content;
        this.hashtags = hashtags;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.me = me;
    }

    public static PostDetailResponse of(Post post, List<PostMedia> postMedias, Long sessionUserId, Long likeId) {
        List<PostMediaInfo> postMediaInfos = postMedias.isEmpty() ? null : postMedias.stream()
                .map(PostMediaInfo::from)
                .collect(Collectors.toList());

        List<PostHashtag> postHashtags = post.getHashtags().getValues();
        List<String> hashtags = (postHashtags.isEmpty()) ? null : postHashtags.stream()
                .map(PostHashtag::getTag)
                .collect(Collectors.toList());

        return PostDetailResponse.builder()
                .id(post.getId())
                .user(UserBasicProfileDto.of(post.getMember()))
                .member(MemberInfo.from(post.getMember()))
                .community(CommunityInfo.from(post.getCommunity()))
                .postMedias(postMediaInfos)
                .likeId(likeId)
                .createdAt(post.getCreatedAt())
                .content(post.getContent())
                .hashtags(hashtags)
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .me(post.isAuthor(sessionUserId))
                .build();
    }

    @Getter
    public static class CommunityInfo {

        private Long id;
        private String name;

        public CommunityInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public static CommunityInfo from(Community community) {
            return new CommunityInfo(community.getId(), community.getCommunityName());
        }
    }

    @Getter
    public static class MemberInfo {

        private Long id;
        private MemberType memberType;

        public MemberInfo(Long id, MemberType memberType) {
            this.id = id;
            this.memberType = memberType;
        }

        public static MemberInfo from(Member member) {
            return new MemberInfo(member.getId(), member.getMemberType());
        }
    }

    @Getter
    public static class PostMediaInfo {

        private MediaType type;
        private String url;

        public PostMediaInfo(MediaType type, String url) {
            this.type = type;
            this.url = url;
        }

        public static PostMediaInfo from(PostMedia postMedia) {
            return new PostMediaInfo(postMedia.getMediaType(), postMedia.getMediaURL());
        }
    }
}
