package boogi.apiserver.domain.community.community.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchCommunityDto {
    private Long id;
    private String name;
    private String description;
    private String createdAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> hashtags;

    private int memberCount;
    private String category;
    private boolean isPrivate;

    private SearchCommunityDto(Community community) {
        this.id = community.getId();
        this.name = community.getCommunityName();
        this.description = community.getDescription();
        this.createdAt = community.getCreatedAt().toString();

        List<CommunityHashtag> hashtags = community.getHashtags();
        if (Objects.nonNull(hashtags) && hashtags.size() > 0) {
            this.hashtags = hashtags.stream()
                    .map(CommunityHashtag::getTag)
                    .collect(Collectors.toList());
        }

        this.memberCount = community.getMemberCount();
        this.category = community.getCategory().toString();
        this.isPrivate = community.isPrivate();
    }

    public static SearchCommunityDto of(Community community) {
        return new SearchCommunityDto(community);
    }
}
