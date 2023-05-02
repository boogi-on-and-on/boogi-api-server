package boogi.apiserver.domain.comment.acceptance;

import boogi.apiserver.domain.comment.dto.dto.UserCommentDto;
import boogi.apiserver.domain.comment.dto.request.CreateCommentRequest;
import boogi.apiserver.utils.AcceptanceTest;
import boogi.apiserver.utils.fixture.CommentFixture;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static boogi.apiserver.utils.fixture.CommentFixture.*;
import static boogi.apiserver.utils.fixture.HttpMethodFixture.*;
import static boogi.apiserver.utils.fixture.PostFixture.createNewPost;
import static boogi.apiserver.utils.fixture.TokenFixture.getSundoToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class CommentAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("댓글을 추가하면, 유저의 댓글 목록에 추가된다.")
    void getUserCommentList() {
        //given
        long postId = createNewPost();
        createNewComment(postId, null);

        //when
        ExtractableResponse<Response> response = httpGet("/comments/users", getSundoToken());

        //then
        List<UserCommentDto> comments = httpGet("/comments/users", getSundoToken())
                .body().jsonPath()
                .getList("comments", UserCommentDto.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(comments)
                .extracting("postId", "content")
                .contains(tuple(postId, "저도 모각코 참여합니다."));
    }

    @Test
    @DisplayName("대댓글에 댓글을 추가할 경우 에러가 발생한다.")
    void maxDepthOverComment() {
        long postId = createNewPost();
        long parentCommentId = createNewComment(postId, null);
        long childCommentId = createNewComment(postId, parentCommentId);

        CreateCommentRequest request3 = new CreateCommentRequest(postId, childCommentId, "모각코 하고싶어요.", new ArrayList<>());
        ExtractableResponse<Response> response = httpPost(request3, "/comments/", getSundoToken());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(getExceptionMessage(response)).isEqualTo("댓글은 대댓글까지만 작성 가능합니다.");
    }

    @Test
    @DisplayName("댓글을 삭제한다.")
    void deleteComment() {
        long newCommentId = createNewComment();

        httpDelete("/comments/" + newCommentId, getSundoToken());

        ExtractableResponse<Response> response = httpGet("/comments/" + newCommentId, getSundoToken());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}
