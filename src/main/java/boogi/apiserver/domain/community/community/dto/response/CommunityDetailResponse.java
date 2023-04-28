package boogi.apiserver.domain.community.community.dto.response;


import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.dto.CommunityDetailInfoDto;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.notice.dto.dto.NoticeDto;
import boogi.apiserver.domain.post.post.dto.dto.LatestCommunityPostDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityDetailResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MemberType sessionMemberType;

    private CommunityDetailInfoDto community;
    private List<NoticeDto> notices;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<LatestCommunityPostDto> posts;

    @Builder(access = AccessLevel.PRIVATE)
    public CommunityDetailResponse(MemberType sessionMemberType, CommunityDetailInfoDto community,
                                   List<NoticeDto> notices, List<LatestCommunityPostDto> posts) {
        this.sessionMemberType = sessionMemberType;
        this.community = community;
        this.notices = notices;
        this.posts = posts;
    }

    public static CommunityDetailResponse of(List<NoticeDto> notices, List<LatestCommunityPostDto> latestPosts, Member member, Community community) {
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
