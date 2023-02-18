package boogi.apiserver.domain.community.community.dto.response;

import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.response.PostOfCommunity;
import boogi.apiserver.global.dto.PaginationDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class CommunityPostsResponse {

    private final String communityName;
    private final List<PostOfCommunity> posts;
    private final PaginationDto pageInfo;
    private final MemberType memberType;

    @Builder(access = AccessLevel.PRIVATE)
    public CommunityPostsResponse(String communityName, List<PostOfCommunity> posts, PaginationDto pageInfo, MemberType memberType) {
        this.communityName = communityName;
        this.posts = posts;
        this.pageInfo = pageInfo;
        this.memberType = memberType;
    }

    public static CommunityPostsResponse of(String communityName, Long userId, Slice<Post> postPage, Member member) {
        List<PostOfCommunity> posts = postPage.getContent()
                .stream()
                .map(p -> new PostOfCommunity(p, userId, member))
                .collect(Collectors.toList());

        final PaginationDto pageInfo = new PaginationDto(postPage);

        final CommunityPostsResponseBuilder builder = CommunityPostsResponse.builder()
                .communityName(communityName)
                .posts(posts)
                .pageInfo(pageInfo);

        if (Objects.nonNull(member) && Objects.nonNull(member.getMemberType())) {
            builder.memberType(member.getMemberType());
        }

        return builder.build();
    }
}
