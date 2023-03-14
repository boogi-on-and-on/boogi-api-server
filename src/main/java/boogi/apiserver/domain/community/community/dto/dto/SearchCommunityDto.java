package boogi.apiserver.domain.community.community.dto.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
public class SearchCommunityDto {
    private Long id;
    private String name;
    private String description;

    @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
    private LocalDateTime createdAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> hashtags;

    private int memberCount;
    private String category;

    @JsonProperty("isPrivate")
    private boolean privated;

    @Builder(access = AccessLevel.PRIVATE)
    public SearchCommunityDto(final Long id, final String name, final String description,
                              final LocalDateTime createdAt, final List<String> hashtags,
                              final int memberCount, final String category, final boolean isPrivate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.hashtags = hashtags;
        this.memberCount = memberCount;
        this.category = category;
        this.privated = isPrivate;
    }

    public static SearchCommunityDto of(Community community) {
        final SearchCommunityDtoBuilder builder = SearchCommunityDto.builder()
                .id(community.getId())
                .name(community.getCommunityName())
                .description(community.getDescription())
                .createdAt(community.getCreatedAt())
                .memberCount(community.getMemberCount())
                .category(community.getCategory().toString())
                .isPrivate(community.isPrivate());

        List<CommunityHashtag> hashtags = community.getHashtags();
        if (Objects.nonNull(hashtags) && hashtags.size() > 0) {
            builder.hashtags(hashtags.stream()
                    .map(CommunityHashtag::getTag)
                    .collect(Collectors.toList())
            );
        }
        return builder.build();
    }

    public static List<SearchCommunityDto> listOf(List<Community> communities) {
        return communities.stream()
                .map(SearchCommunityDto::of)
                .collect(Collectors.toList());
    }
}
