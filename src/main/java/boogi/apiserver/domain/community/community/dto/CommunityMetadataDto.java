package boogi.apiserver.domain.community.community.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommunityMetadataDto {
    private String name;
    private String introduce;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> hashtags;

    private CommunityMetadataDto(Community community) {
        this.name = community.getCommunityName();
        this.introduce = community.getDescription();

        List<CommunityHashtag> hashtags = community.getHashtags();
        if (hashtags != null && hashtags.size() > 0) {
            this.hashtags = hashtags.stream()
                    .filter(h -> h.getCanceledAt() == null)
                    .map(CommunityHashtag::getTag)
                    .collect(Collectors.toList());
        }
    }

    public static CommunityMetadataDto of(Community community) {
        return new CommunityMetadataDto(community);
    }
}
