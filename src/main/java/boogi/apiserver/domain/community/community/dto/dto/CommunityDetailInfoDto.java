package boogi.apiserver.domain.community.community.dto.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
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
public class CommunityDetailInfoDto {
    private Boolean isPrivated;
    private String category;
    private String name;
    private String introduce;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> hashtags;
    private String memberCount;

    @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
    private LocalDateTime createdAt;

    @Builder(access = AccessLevel.PRIVATE)
    public CommunityDetailInfoDto(Boolean isPrivated, String category, String name, String introduce,
                                  List<String> hashtags, String memberCount, LocalDateTime createdAt) {
        this.isPrivated = isPrivated;
        this.category = category;
        this.name = name;
        this.introduce = introduce;
        this.hashtags = hashtags;
        this.memberCount = memberCount;
        this.createdAt = createdAt;
    }

    public static CommunityDetailInfoDto of(Community community) {
        List<CommunityHashtag> communityHashtags = community.getHashtags();
        List<String> hashtags = (communityHashtags != null && communityHashtags.size() > 0) ?
                communityHashtags.stream()
                        .map(CommunityHashtag::getTag)
                        .collect(Collectors.toList()) : null;

        return CommunityDetailInfoDto.builder()
                .isPrivated(community.isPrivate())
                .name(community.getCommunityName())
                .introduce(community.getDescription())
                .hashtags(hashtags)
                .memberCount(String.valueOf(community.getMemberCount()))
                .createdAt(community.getCreatedAt())
                .category(community.getCategory().toString())
                .build();
    }
}
