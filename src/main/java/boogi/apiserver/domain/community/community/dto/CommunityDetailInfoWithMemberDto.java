package boogi.apiserver.domain.community.community.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import boogi.apiserver.domain.member.domain.Member;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
public class CommunityDetailInfoWithMemberDto {
    private Boolean isJoined;
    private Boolean isPrivated;
    private String name;
    private String introduce;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> hashtags;
    private String memberCount;
    private String createdAt;

    private CommunityDetailInfoWithMemberDto(Member member, Community community, List<CommunityHashtag> hashtags) {
        this.isJoined = member != null;
        this.isPrivated = community.isPrivate();
        this.name = community.getCommunityName();
        this.introduce = community.getDescription();

        if (hashtags.getClass().isArray() && hashtags.size() > 0) {
            this.hashtags = hashtags.stream()
                    .map(CommunityHashtag::getTag)
                    .collect(Collectors.toList());
        }
        this.memberCount = String.valueOf(community.getMemberCount());
        this.createdAt = community.getCreatedAt().toString();
    }

    public static CommunityDetailInfoWithMemberDto of(Member member, Community community) {
        return new CommunityDetailInfoWithMemberDto(member, community, community.getHashtags());
    }
}
