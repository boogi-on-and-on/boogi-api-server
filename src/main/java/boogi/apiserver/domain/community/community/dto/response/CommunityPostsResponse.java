package boogi.apiserver.domain.community.community.dto.response;

import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.dto.CommunityPostDto;
import boogi.apiserver.global.dto.PaginationDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPostsResponse {

    private String communityName;
    private List<CommunityPostDto> posts;
    private PaginationDto pageInfo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MemberType memberType;

    @Builder(access = AccessLevel.PRIVATE)
    public CommunityPostsResponse(String communityName, List<CommunityPostDto> posts, PaginationDto pageInfo, MemberType memberType) {
        this.communityName = communityName;
        this.posts = posts;
        this.pageInfo = pageInfo;
        this.memberType = memberType;
    }

    public static CommunityPostsResponse of(String communityName, Long userId, Slice<Post> postPage, Member member) {
        List<CommunityPostDto> posts = postPage.getContent()
                .stream()
                .map(p -> CommunityPostDto.of(p, userId, member))
                .collect(Collectors.toList());

        final PaginationDto pageInfo = PaginationDto.of(postPage);

        final CommunityPostsResponseBuilder builder = CommunityPostsResponse.builder()
                .communityName(communityName)
                .posts(posts)
                .pageInfo(pageInfo);

        if (member != null && member.getMemberType() != null) {
            builder.memberType(member.getMemberType());
        }

        return builder.build();
    }
}