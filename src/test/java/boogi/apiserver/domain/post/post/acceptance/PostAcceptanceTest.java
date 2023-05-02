package boogi.apiserver.domain.post.post.acceptance;

import boogi.apiserver.domain.post.post.dto.dto.SearchPostDto;
import boogi.apiserver.domain.post.post.dto.dto.UserPostDto;
import boogi.apiserver.domain.post.post.dto.request.UpdatePostRequest;
import boogi.apiserver.domain.post.post.dto.response.PostDetailResponse;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.utils.AcceptanceTest;
import boogi.apiserver.utils.fixture.UserFixture;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static boogi.apiserver.utils.fixture.HttpMethodFixture.*;
import static boogi.apiserver.utils.fixture.PostFixture.createNewPost;
import static boogi.apiserver.utils.fixture.TokenFixture.getSundoToken;
import static boogi.apiserver.utils.fixture.TokenFixture.getYongjinToken;
import static org.assertj.core.api.Assertions.assertThat;

public class PostAcceptanceTest extends AcceptanceTest {


    @Test
    @DisplayName("게시글을 수정하면 변경된 상세 정보가 조회된다.")
    void createPost() {
        //given
        long postId = createNewPost();

        UpdatePostRequest postUpdateRequest = new UpdatePostRequest("변경된 게시글입니다!!", List.of("코딩", "컴공"), new ArrayList<>());
        httpPatch(postUpdateRequest, "/posts/" + postId, getSundoToken());

        //when
        ExtractableResponse<Response> response = httpGet("/posts/" + postId, getSundoToken());

        //then
        PostDetailResponse parsed = response.body().as(PostDetailResponse.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(parsed.getId()).isEqualTo(postId);
        assertThat(parsed.getContent()).isEqualTo("변경된 게시글입니다!!");
        assertThat(parsed.getHashtags()).containsExactlyInAnyOrder("코딩", "컴공");

    }

    @Test
    @DisplayName("게시글을 삭제하면 게시글 상세조회를 할 수 없다.")
    void deletePost() {
        //given
        long postId = createNewPost();

        //when
        httpDelete("/posts/" + postId, getSundoToken());

        //then
        ExtractableResponse<Response> response = httpGet("/posts/" + postId, getSundoToken());
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("게시글을 추가하면, 유저의 게시글 목록에 추가된다.")
    void getUserPostList() {
        //given
        long postId = createNewPost();

        //when
        ExtractableResponse<Response> response = httpGet("/posts/users?userId=" + UserFixture.SUNDO_ID, getSundoToken());

        //then
        List<UserPostDto> posts = response.body().jsonPath().getList("posts", UserPostDto.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(posts)
                .extracting("id")
                .contains(postId);
    }

    @Test
    @DisplayName("게시글 좋아요를 누르면, 게시글의 좋아요 개수가 1 증가한다.")
    void doLikeToPost1() {
        //given
        long postId = createNewPost();
        int prevLikeCount = httpGet("/posts/" + postId, getYongjinToken())
                .body()
                .jsonPath()
                .getInt("likeCount");

        httpPost("/posts/" + postId + "/likes", getYongjinToken());

        //when
        ExtractableResponse<Response> response = httpGet("/posts/" + postId, getYongjinToken());

        //then
        int newLikeCount = response.body()
                .jsonPath()
                .getInt("likeCount");

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(prevLikeCount + 1).isEqualTo(newLikeCount);
    }

    @Test
    @DisplayName("게시글 좋아요를 누르면, 좋아요 목록에 유저가 추가된다.")
    void doLikeToPost2() {
        //given
        long postId = createNewPost();

        httpPost("/posts/" + postId + "/likes", getYongjinToken());

        //when
        ExtractableResponse<Response> response = httpGet("/posts/" + postId + "/likes", getYongjinToken());

        //then
        List<UserBasicProfileDto> users = response.body().jsonPath()
                .getList("members", UserBasicProfileDto.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(users)
                .extracting("id")
                .contains(UserFixture.YONGJIN_ID);
    }

    @Test
    @DisplayName("게시글 해시테그와 검색 키워드가 같으면 검색 결과에 게시글이 조회된다.")
    void searchByHashtag() {
        //given
        long postId = createNewPost();

        //when
        ExtractableResponse<Response> response = httpGet("/posts/search?keyword=" + "모각코", getSundoToken());

        //then
        List<SearchPostDto> posts = response.body().jsonPath()
                .getList("posts", SearchPostDto.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(posts)
                .extracting("id")
                .contains(postId);
    }
}
