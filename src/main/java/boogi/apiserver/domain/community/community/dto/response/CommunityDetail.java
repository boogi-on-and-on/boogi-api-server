package boogi.apiserver.domain.community.community.dto.response;


import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.notice.dto.response.NoticeDto;
import boogi.apiserver.domain.post.post.dto.response.LatestPostOfCommunityDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CommunityDetail {

    private MemberType sessionMemberType;
    private CommunityDetailInfoDto community;
    private List<NoticeDto> notices;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<LatestPostOfCommunityDto> posts;

    public static CommunityDetail of(MemberType sessionMemberType,
                                     CommunityDetailInfoDto community,
                                     List<NoticeDto> communityNotices,
                                     List<LatestPostOfCommunityDto> posts) {
        return CommunityDetail.builder()
                .sessionMemberType(sessionMemberType)
                .community(community)
                .notices(communityNotices)
                .posts(posts)
                .build();
    }
}
