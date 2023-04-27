package boogi.apiserver.domain.community.community.acceptance;

import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.dto.request.CreateCommunityRequest;
import boogi.apiserver.utils.AcceptanceTest;
import boogi.apiserver.utils.fixture.HttpMethodFixture;
import boogi.apiserver.utils.fixture.TokenFixture;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CommunityAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("커뮤니티를 생성한다.")
    void createCommunity() {
        String token = TokenFixture.getSundoToken();
        CreateCommunityRequest request = new CreateCommunityRequest("축구 커뮤니티", CommunityCategory.HOBBY.toString(),
                "축구를 즐기면서 합시다!", List.of("메시"), false, false);

        ExtractableResponse<Response> response = HttpMethodFixture.httpPost(request, "/communities/", token);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("이미 같은 이름으로 커뮤니티가 있는 경우 생성에 실패한다.")
    void alreadySameName() {
        String token = TokenFixture.getSundoToken();
        CreateCommunityRequest request1 = new CreateCommunityRequest("축구 커뮤니티", CommunityCategory.HOBBY.toString(),
                "축구를 즐기면서 합시다!", List.of("메시"), false, false);

        CreateCommunityRequest request2 = new CreateCommunityRequest("축구 커뮤니티", CommunityCategory.HOBBY.toString(),
                "축구를 즐기면서 합시다!", List.of("메시"), false, false);

        HttpMethodFixture.httpPost(request1, "/communities/", token);
        ExtractableResponse<Response> response = HttpMethodFixture.httpPost(request2, "/communities/", token);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(HttpMethodFixture.getExceptionMessage(response))
                .isEqualTo("이미 해당 커뮤니티 이름이 존재합니다.");
    }
}
