package boogi.apiserver.domain.post.post.dto.dto;

import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.dto.dto.PostMediaMetadataDto;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class CommunityPostDto {

    private Long id;
    private UserBasicProfileDto user;
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

    @Builder(access = AccessLevel.PRIVATE)
    public CommunityPostDto(Long id, UserBasicProfileDto user, LocalDateTime createdAt, String content,
                            List<String> hashtags, List<PostMediaMetadataDto> postMedias, Integer likeCount,
                            Integer commentCount, Boolean me, Long likeId) {
        this.id = id;
        this.user = user;
        this.createdAt = createdAt;
        this.content = content;
        this.hashtags = hashtags;
        this.postMedias = postMedias;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.me = me;
        this.likeId = likeId;
    }

    public static CommunityPostDto of(Post post, Long userId, Member sessionMember) {
        List<PostHashtag> postHashtags = post.getHashtags().getValues();
        List<String> hashtags = (postHashtags == null || postHashtags.size() == 0) ? null :
                postHashtags.stream()
                        .map(PostHashtag::getTag)
                        .collect(Collectors.toList());

        List<PostMedia> postMedias = post.getPostMedias().getValues();
        List<PostMediaMetadataDto> postMediaMetadataDtos = (postMedias == null || postMedias.size() == 0) ? null :
                postMedias.stream()
                        .map(PostMediaMetadataDto::from)
                        .collect(Collectors.toList());

        List<Like> likes = post.getLikes();
        Long likeId = null;
        if (Objects.nonNull(sessionMember) && likes != null && likes.size() > 0) {
            likeId = likes.stream()
                    .filter(l -> l.getMember().getId().equals(sessionMember.getId()))
                    .findFirst()
                    .map(Like::getId)
                    .orElse(null);
        }

        return CommunityPostDto.builder()
                .id(post.getId())
                .user(UserBasicProfileDto.from(post.getMember()))
                .createdAt(post.getCreatedAt())
                .content(post.getContent())
                .hashtags(hashtags)
                .postMedias(postMediaMetadataDtos)
                .likeId(likeId)
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .me(post.isAuthor(userId))
                .build();
    }
}
