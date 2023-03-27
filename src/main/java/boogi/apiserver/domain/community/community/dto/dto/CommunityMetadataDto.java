package boogi.apiserver.domain.community.community.dto.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
public class CommunityMetadataDto {
    private String name;
    private String introduce;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> hashtags;

    @Builder(access = AccessLevel.PRIVATE)
    public CommunityMetadataDto(String name, String introduce, List<String> hashtags) {
        this.name = name;
        this.introduce = introduce;
        this.hashtags = hashtags;
    }

    public static CommunityMetadataDto of(Community community) {
        final CommunityMetadataDtoBuilder builder = CommunityMetadataDto.builder()
                .introduce(community.getDescription())
                .name(community.getCommunityName());

        final List<CommunityHashtag> hashtags = community.getHashtags();
        if (hashtags != null && hashtags.size() > 0) {
            List<String> tags = hashtags.stream()
                    .map(CommunityHashtag::getTag)
                    .collect(Collectors.toList());
            builder.hashtags(tags);
        }

        return builder.build();
    }
}
