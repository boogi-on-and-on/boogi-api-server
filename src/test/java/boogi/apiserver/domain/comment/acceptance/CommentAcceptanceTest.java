package boogi.apiserver.domain.comment.acceptance;

import boogi.apiserver.domain.comment.dto.dto.UserCommentDto;
import boogi.apiserver.domain.comment.dto.request.CreateCommentRequest;
import boogi.apiserver.utils.AcceptanceTest;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static boogi.apiserver.utils.fixture.HttpMethodFixture.httpGet;
import static boogi.apiserver.utils.fixture.HttpMethodFixture.httpPost;
import static boogi.apiserver.utils.fixture.PostFixture.createNewPost;
import static boogi.apiserver.utils.fixture.TokenFixture.getSundoToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class CommentAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("게시글을 추가하면, 유저의 댓글 목록에 추가된다.")
    void getUserCommentList() {
        //given
        long postId = createNewPost();

        CreateCommentRequest request = new CreateCommentRequest(postId, null, "모각코 하고싶어요.", new ArrayList<>());
        httpPost(request, "/comments/", getSundoToken());

        //when
        ExtractableResponse<Response> response = httpGet("/comments/users", getSundoToken());

        //then
        List<UserCommentDto> comments = httpGet("/comments/users", getSundoToken())
                .body().jsonPath()
                .getList("comments", UserCommentDto.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(comments)
                .extracting("postId", "content")
                .contains(tuple(postId, "모각코 하고싶어요."));
    }
}
