package boogi.apiserver.utils.fixture;

import boogi.apiserver.domain.comment.dto.request.CreateCommentRequest;

import java.util.ArrayList;

import static boogi.apiserver.utils.fixture.HttpMethodFixture.httpPost;
import static boogi.apiserver.utils.fixture.PostFixture.createNewPost;
import static boogi.apiserver.utils.fixture.TokenFixture.getSundoToken;

public enum CommentFixture {
    ;

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
