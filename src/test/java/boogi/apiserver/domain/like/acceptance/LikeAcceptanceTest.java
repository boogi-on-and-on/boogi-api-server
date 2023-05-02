package boogi.apiserver.domain.like.acceptance;

import boogi.apiserver.domain.comment.dto.response.CommentsAtPostResponse;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.utils.AcceptanceTest;
import boogi.apiserver.utils.fixture.UserFixture;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static boogi.apiserver.utils.fixture.CommentFixture.createNewComment;
import static boogi.apiserver.utils.fixture.HttpMethodFixture.*;
import static boogi.apiserver.utils.fixture.PostFixture.createNewPost;
import static boogi.apiserver.utils.fixture.TokenFixture.getSundoToken;
import static boogi.apiserver.utils.fixture.TokenFixture.getYongjinToken;
import static org.assertj.core.api.Assertions.assertThat;

public class LikeAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("게시글 좋아요를 취소하면, 게시글의 좋아요 개수가 1 감소한다.")
    void doUnlikeToPost1() {
        //given
        long postId = createNewPost();
        long likeId = httpPost("/posts/" + postId + "/likes", getYongjinToken())
                .body().jsonPath()
                .getLong("id");

        int prevLikeCount = httpGet("/posts/" + postId, getYongjinToken())
                .body()
                .jsonPath()
                .getInt("likeCount");

        //when
        httpDelete("/likes/" + likeId, getYongjinToken());

        //then
        ExtractableResponse<Response> response = httpGet("/posts/" + postId, getYongjinToken());
        int newLikeCount = response.body()
                .jsonPath()
                .getInt("likeCount");

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(newLikeCount).isEqualTo(prevLikeCount - 1);

    }

    @Test
    @DisplayName("게시글 좋아요를 취소하면, 좋아요 목록에 유저가 제거된다.")
    void doUnlikeToPost2() {
        //given
        long postId = createNewPost();
        long likeId = httpPost("/posts/" + postId + "/likes", getYongjinToken())
                .body().jsonPath()
                .getLong("id");

        //when
        httpDelete("/likes/" + likeId, getYongjinToken());

        //then
        ExtractableResponse<Response> response = httpGet("/posts/" + postId + "/likes", getYongjinToken());
        List<UserBasicProfileDto> users = response.body().jsonPath()
                .getList("members", UserBasicProfileDto.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(users)
                .extracting("id")
                .doesNotContain(UserFixture.YONGJIN_ID);
    }

    @Test
    @DisplayName("댓글 좋아요를 취소하면, 댓글의 좋아요 개수가 1 감소한다.")
    void doUnlikeToComment1() {
        //given
        long postId = createNewPost();
        long commentId = createNewComment(postId, null);

        int likeId = httpPost("/comments/" + commentId + "/likes", getSundoToken())
                .body().jsonPath()
                .getInt("id");

        Long prevCommentLikeCount = httpGet("/posts/" + postId + "/comments", getSundoToken())
                .body().jsonPath()
                .getList("comments", CommentsAtPostResponse.ParentCommentInfo.class)
                .stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst().get()
                .getLikeCount();

        //when
        ExtractableResponse<Response> response = httpDelete("/likes/" + likeId, getSundoToken());

        //then
        Long newCommentLikeCount = httpGet("/posts/" + postId + "/comments", getSundoToken())
                .body().jsonPath()
                .getList("comments", CommentsAtPostResponse.ParentCommentInfo.class)
                .stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst().get()
                .getLikeCount();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(newCommentLikeCount).isEqualTo(prevCommentLikeCount - 1);
    }

    @Test
    @DisplayName("댓글 좋아요를 취소하면, 게시글 댓글의 목록에 유저가 제거된다.")
    void doUnlikeToComment2() {
        //given
        long postId = createNewPost();
        long commentId = createNewComment(postId, null);

        int likeId = httpPost("/comments/" + commentId + "/likes", getSundoToken())
                .body().jsonPath()
                .getInt("id");

        //when
        ExtractableResponse<Response> response = httpDelete("/likes/" + likeId, getSundoToken());

        //then
        List<UserBasicProfileDto> users = httpGet("/comments/" + commentId + "/likes", getSundoToken())
                .body().jsonPath()
                .getList("members", UserBasicProfileDto.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(users)
                .extracting("id")
                .doesNotContain(UserFixture.SUNDO_ID);
    }
}
