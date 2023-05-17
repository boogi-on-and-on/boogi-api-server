package boogi.apiserver.utils.fixture;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.request.CreatePostRequest;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.utils.TestTimeReflection;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static boogi.apiserver.utils.fixture.CommunityFixture.POCS_ID;
import static boogi.apiserver.utils.fixture.HttpMethodFixture.httpPost;
import static boogi.apiserver.utils.fixture.TimeFixture.STANDARD;
import static boogi.apiserver.utils.fixture.TokenFixture.getSundoToken;

public enum PostFixture {
    POST1("안녕하세요 가입인사드립니다.", null, 1, 1, STANDARD),
    POST2("이번주 모임은 17시입니다.", null, 2, 2, STANDARD.minusDays(1)),
    POST3("저는 이번주에 바뻐서 못할 것 같습니다.", null, 3, 3, STANDARD.minusDays(2)),
    DELETED_POST4("다음주에 야구 보러 갈 사람?", STANDARD, 3, 3, STANDARD.minusDays(3));

    public final String content;
    public final LocalDateTime deletedAt;
    public final int likeCount;
    public final int commentCount;
    public final LocalDateTime createdAt;

    PostFixture(String content, LocalDateTime deletedAt, int likeCount, int commentCount, LocalDateTime createdAt) {
        this.content = content;
        this.deletedAt = deletedAt;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
    }

    public Post toPost(Member member, Community community, List<String> tags, List<PostMedia> postMedias) {
        return toPost(null, member, community, tags, postMedias);
    }

    public Post toPost(Long id, Member member, Community community, List<String> tags, List<PostMedia> postMedias) {
        Post post = Post.builder()
                .id(id)
                .community(community)
                .member(member)
                .content(this.content)
                .deletedAt(this.deletedAt)
                .likeCount(this.likeCount)
                .commentCount(this.commentCount)
                .hashtags(new ArrayList<>())
                .postMedias(new ArrayList<>())
                .build();
        post.addTags(tags);
        post.addPostMedias(Objects.requireNonNullElse(postMedias, new ArrayList<>()));
        TestTimeReflection.setCreatedAt(post, this.createdAt);
        return post;
    }

    public static long createNewPost() {
        CreatePostRequest request = new CreatePostRequest(POCS_ID, "같이 모각코 하실분 구합니다.",
                List.of("모각코", "모집"), new ArrayList<>(), new ArrayList<>());

        return httpPost(request, "/posts/", getSundoToken())
                .body().jsonPath().getLong("id");
    }
}
