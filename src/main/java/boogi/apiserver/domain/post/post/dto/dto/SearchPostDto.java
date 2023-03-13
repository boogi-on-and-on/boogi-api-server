package boogi.apiserver.domain.post.post.dto.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
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
import java.util.stream.Collectors;

@Getter
public class SearchPostDto {

    private Long id;
    private UserBasicProfileDto user;
    private Long communityId;
    private String communityName;

    @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
    private LocalDateTime createdAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> hashtags;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PostMediaMetadataDto> postMedias;

    private int commentCount;
    private int likeCount;

    private String content;


    @Builder(access = AccessLevel.PRIVATE)
    public SearchPostDto(Long id, UserBasicProfileDto user, Long communityId, String communityName,
                         LocalDateTime createdAt, List<String> hashtags, List<PostMediaMetadataDto> postMedias,
                         int commentCount, int likeCount, String content) {
        this.id = id;
        this.user = user;
        this.communityId = communityId;
        this.communityName = communityName;
        this.createdAt = createdAt;
        this.hashtags = hashtags;
        this.postMedias = postMedias;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.content = content;
    }

    public static SearchPostDto from(Post post) {
        Community community = post.getCommunity();

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

        return SearchPostDto.builder()
                .id(post.getId())
                .user(UserBasicProfileDto.from(post.getMember()))
                .communityId(community.getId())
                .communityName(community.getCommunityName())
                .createdAt(post.getCreatedAt())
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikeCount())
                .content(post.getContent())
                .hashtags(hashtags)
                .postMedias(postMediaMetadataDtos)
                .build();
    }
}

