package boogi.apiserver.utils.fixture;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.request.CreateCommentRequest;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.utils.TestTimeReflection;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static boogi.apiserver.utils.fixture.HttpMethodFixture.httpPost;
import static boogi.apiserver.utils.fixture.PostFixture.createNewPost;
import static boogi.apiserver.utils.fixture.TimeFixture.STANDARD;
import static boogi.apiserver.utils.fixture.TokenFixture.getSundoToken;

public enum CommentFixture {
    COMMENT1("만나서 반갑습니다~!.", false, null, STANDARD),
    COMMENT2("저도 이번 모임 참가예정입니다.", false, null, STANDARD.minusDays(1)),
    COMMENT3("저도 가고싶습니다. 하지만 못가네요", true, null, STANDARD.minusDays(2)),
    COMMENT4("저는 보러 못갑니다. 아쉽네요", false, STANDARD, STANDARD.minusDays(3));

    public final String content;
    public final boolean child;
    public final LocalDateTime deletedAt;
    public final LocalDateTime createdAt;

    CommentFixture(String content, boolean child, LocalDateTime deletedAt, LocalDateTime createdAt) {
        this.content = content;
        this.child = child;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
    }

    public Comment toComment(Long id, Post post, Member member, Comment parent) {
        Comment comment = Comment.builder()
                .id(id)
                .post(post)
                .member(member)
                .parent(parent)
                .content(this.content)
                .child(this.child)
                .deletedAt(this.deletedAt)
                .build();
        TestTimeReflection.setCreatedAt(comment, this.createdAt);
        return comment;
    }

    public Comment toComment(Post post, Member member, Comment parent) {
        return toComment(null, post, member, parent);
    }

    public static long createNewComment() {
        long postId = createNewPost();
        return createNewComment(postId, null);
    }

    public static long createNewComment(Long postId, Long parentCommentId) {
        CreateCommentRequest request = new CreateCommentRequest(postId, parentCommentId, "저도 모각코 참여합니다.", new ArrayList<>());

        return httpPost(request, "/comments/", getSundoToken())
                .body().jsonPath().getLong("id");
    }
}
