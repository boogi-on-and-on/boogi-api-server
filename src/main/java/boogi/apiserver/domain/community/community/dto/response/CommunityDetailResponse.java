package boogi.apiserver.domain.community.community.dto.response;


import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.dto.CommunityDetailInfoDto;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.notice.dto.response.NoticeDto;
import boogi.apiserver.domain.post.post.dto.response.LatestPostOfCommunityDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
public class CommunityDetailResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final MemberType sessionMemberType;

    private final CommunityDetailInfoDto community;
    private final List<NoticeDto> notices;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final List<LatestPostOfCommunityDto> posts;

    @Builder(access = AccessLevel.PRIVATE)
    public CommunityDetailResponse(MemberType sessionMemberType, CommunityDetailInfoDto community,
                                   List<NoticeDto> notices, List<LatestPostOfCommunityDto> posts) {
        this.sessionMemberType = sessionMemberType;
        this.community = community;
        this.notices = notices;
        this.posts = posts;
    }

    public static CommunityDetailResponse of(List<NoticeDto> notices, List<LatestPostOfCommunityDto> latestPosts, Member member, Community community) {
        final CommunityDetailResponseBuilder builder = CommunityDetailResponse.builder()
                .notices(notices)
                .posts(latestPosts)
                .community(CommunityDetailInfoDto.of(community));

        if (Objects.nonNull(member)) {
            builder.sessionMemberType(member.getMemberType());
        }

        return builder.build();
    }
}
