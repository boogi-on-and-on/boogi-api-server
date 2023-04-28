package boogi.apiserver.domain.community.community.acceptance;

import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.dto.dto.*;
import boogi.apiserver.domain.community.community.dto.request.CommunitySettingRequest;
import boogi.apiserver.domain.community.community.dto.request.CreateCommunityRequest;
import boogi.apiserver.domain.community.community.dto.request.JoinRequestIdsRequest;
import boogi.apiserver.domain.community.community.dto.request.UpdateCommunityRequest;
import boogi.apiserver.domain.community.community.dto.response.CommunityDetailResponse;
import boogi.apiserver.domain.member.dto.dto.BannedMemberDto;
import boogi.apiserver.domain.post.post.dto.dto.CommunityPostDto;
import boogi.apiserver.domain.post.post.dto.request.CreatePostRequest;
import boogi.apiserver.utils.AcceptanceTest;
import boogi.apiserver.utils.fixture.*;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static boogi.apiserver.utils.fixture.TokenFixture.getSundoToken;
import static boogi.apiserver.utils.fixture.TokenFixture.getYongjinToken;
import static org.assertj.core.api.Assertions.assertThat;

public class CommunityAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("커뮤니티를 생성한다.")
    void createCommunity() {
        String token = getSundoToken();
        CreateCommunityRequest request = new CreateCommunityRequest("축구 커뮤니티", CommunityCategory.HOBBY.toString(),
                "축구를 즐기면서 합시다!", List.of("메시"), false, false);

        ExtractableResponse<Response> response = HttpMethodFixture.httpPost(request, "/communities/", token);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("이미 같은 이름으로 커뮤니티가 있는 경우 생성에 실패한다.")
    void alreadySameName() {
        String token = getSundoToken();

        String url = "/communities/";

        CreateCommunityRequest request1 = new CreateCommunityRequest("축구 커뮤니티", CommunityCategory.HOBBY.toString(),
                "축구를 즐기면서 합시다!", List.of("메시"), false, false);

        CreateCommunityRequest request2 = new CreateCommunityRequest("축구 커뮤니티", CommunityCategory.HOBBY.toString(),
                "축구를 즐기면서 합시다!", List.of("메시"), false, false);

        HttpMethodFixture.httpPost(request1, url, token);
        ExtractableResponse<Response> response = HttpMethodFixture.httpPost(request2, url, token);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(HttpMethodFixture.getExceptionMessage(response))
                .isEqualTo("이미 해당 커뮤니티 이름이 존재합니다.");
    }

    @Test
    @DisplayName("커뮤니티 정보 변경 후, 조회 시 정보가 변경되어 조회된다.")
    void updateCommunity() {
        String token = getSundoToken();

        String url = "/communities/" + CommunityFixture.POCS_ID;

        UpdateCommunityRequest request = new UpdateCommunityRequest("안녕하세요! 반갑습니다~!", List.of("컴공학술동아리"));
        HttpMethodFixture.httpPatch(request, url, token);

        ExtractableResponse<Response> response = HttpMethodFixture.httpGet(url, token);
        CommunityDetailResponse parsed = response.body().as(CommunityDetailResponse.class);


        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        CommunityDetailInfoDto communityDto = parsed.getCommunity();
        assertThat(communityDto.getIntroduce()).isEqualTo("안녕하세요! 반갑습니다~!");
        assertThat(communityDto.getHashtags()).containsExactly("컴공학술동아리");
    }

    @Test
    @DisplayName("커뮤니티 설정 정보 변경 시, 변경된 결과가 조회한다.")
    void changeSetting() {
        String token = getSundoToken();

        String url = "/communities/" + CommunityFixture.POCS_ID + "/settings";

        CommunitySettingRequest request = new CommunitySettingRequest(false, true);
        HttpMethodFixture.httpPost(request, url, token);

        ExtractableResponse<Response> response = HttpMethodFixture.httpGet(url, token);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        CommunitySettingInfoDto parsed = response.body().jsonPath().getObject("settingInfo", CommunitySettingInfoDto.class);
        assertThat(parsed.getIsSecret()).isFalse();
        assertThat(parsed.getIsAuto()).isTrue();
    }

    @Test
    @DisplayName("게시글 추가 시, 커뮤니티의 글 목록에 첫 번째로 조회된다.")
    void post() {
        String token = getSundoToken();

        Long communityId = CommunityFixture.POCS_ID;

        CreatePostRequest request = new CreatePostRequest(communityId, "오늘도 열심히 코딩하자", List.of("모각코"), new ArrayList<>(), new ArrayList<>());
        ExtractableResponse<Response> postResponse = HttpMethodFixture.httpPost(request, "/posts/", token);
        int newPostId = postResponse.body().jsonPath().getInt("id");

        ExtractableResponse<Response> response = HttpMethodFixture.httpGet("/communities/" + communityId + "/posts", token);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<CommunityPostDto> posts = response.body().jsonPath().getList("posts", CommunityPostDto.class);
        assertThat(posts.get(0).getId()).isEqualTo(newPostId);
    }

    @Test
    @DisplayName("멤버를 차단하면, 차단목록에 추가된다.")
    void ban() {
        Long bannedMemberId = MemberFixture.YONGJIN_POCS_ID;
        HttpMethodFixture.httpPost("/members/" + bannedMemberId + "/ban", getYongjinToken());

        ExtractableResponse<Response> response = HttpMethodFixture.httpGet("/communities/" + CommunityFixture.POCS_ID + "/members/banned", getSundoToken());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<BannedMemberDto> parsed = response.body().jsonPath().getList("banned", BannedMemberDto.class);
        assertThat(parsed.get(0).getMemberId()).isEqualTo(bannedMemberId);
    }

    @Test
    @DisplayName("가입요청을 추가하면, 목록에 추가된다.")
    void joinRequests() {
        String url = "/communities/" + CommunityFixture.ENGLISH_ID + "/requests";
        long requestId = HttpMethodFixture.httpPost(url, getSundoToken())
                .body().jsonPath()
                .getLong("id");

        ExtractableResponse<Response> response = HttpMethodFixture.httpGet(url, getYongjinToken());

        List<UserJoinRequestInfoDto> requests = response.body().jsonPath().getList("requests", UserJoinRequestInfoDto.class);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(requests.get(0).getId()).isEqualTo(requestId);
    }

    @Test
    @DisplayName("가입요청 수락 시, 멤버로 추가된다")
    void confirm() {
        Long communityId = CommunityFixture.ENGLISH_ID;

        long requestId = HttpMethodFixture.httpPost("/communities/" + communityId + "/requests", getSundoToken())
                .body().jsonPath()
                .getLong("id");

        JoinRequestIdsRequest confirmRequest = new JoinRequestIdsRequest(List.of(requestId));
        HttpMethodFixture.httpPost(confirmRequest, "/communities/" + communityId + "/requests/confirm", getYongjinToken());

        ExtractableResponse<Response> response = HttpMethodFixture.httpGet("/communities/" + communityId + "/members", getYongjinToken());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<JoinedMemberInfoDto> members = response.body().jsonPath().getList("members", JoinedMemberInfoDto.class);
        assertThat(members)
                .extracting("user")
                .extracting("id")
                .contains(UserFixture.SUNDO_ID);
    }

    @Test
    @DisplayName("가입요청을 거절하면 가입요청 목록에 조회되지 않는다.")
    void reject() {
        Long communityId = CommunityFixture.ENGLISH_ID;

        long requestId = HttpMethodFixture.httpPost("/communities/" + communityId + "/requests", getSundoToken())
                .body().jsonPath()
                .getLong("id");

        JoinRequestIdsRequest confirmRequest = new JoinRequestIdsRequest(List.of(requestId));
        HttpMethodFixture.httpPost(confirmRequest, "/communities/" + communityId + "/requests/reject", getYongjinToken());

        ExtractableResponse<Response> response = HttpMethodFixture.httpGet("/communities/" + communityId + "/requests", getYongjinToken());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<UserJoinRequestInfoDto> requests = response.body().jsonPath().getList("requests", UserJoinRequestInfoDto.class);
        assertThat(requests.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("커뮤니티 이름에 검색 글자가 포함되면 조회된다.")
    void searchByName() {
        ExtractableResponse<Response> response = HttpMethodFixture.httpGet("/communities/search?keyword=컴퓨터", getSundoToken());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<SearchCommunityDto> communities = response.body().jsonPath().getList("communities", SearchCommunityDto.class);
        assertThat(communities)
                .extracting("id")
                .contains(CommunityFixture.POCS_ID);
    }

    @Test
    @DisplayName("커뮤니티 해시태그에 검색 글자가 포함되면 조회된다.")
    void searchByTag() {
        UpdateCommunityRequest request = new UpdateCommunityRequest("안녕하세요! 반갑습니다~!", List.of("코딩하자"));
        HttpMethodFixture.httpPatch(request, "/communities/" + CommunityFixture.POCS_ID, getSundoToken());

        ExtractableResponse<Response> response = HttpMethodFixture.httpGet("/communities/search?keyword=코딩하자", getSundoToken());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<SearchCommunityDto> communities = response.body().jsonPath().getList("communities", SearchCommunityDto.class);
        assertThat(communities)
                .extracting("id")
                .contains(CommunityFixture.POCS_ID);
    }
}
