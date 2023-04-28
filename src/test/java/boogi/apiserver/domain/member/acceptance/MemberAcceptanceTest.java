package boogi.apiserver.domain.member.acceptance;

import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.utils.AcceptanceTest;


import boogi.apiserver.utils.fixture.CommunityFixture;
import boogi.apiserver.utils.fixture.UserFixture;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static boogi.apiserver.utils.fixture.HttpMethodFixture.httpGet;
import static boogi.apiserver.utils.fixture.TokenFixture.getSundoToken;
import static org.assertj.core.api.Assertions.assertThat;

public class MemberAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("맨션할 멤버 목록을 조회한다.")
    void mention() {
        ExtractableResponse<Response> response = httpGet("/members/search/mention?name=선도&communityId=" + CommunityFixture.POCS_ID,
                getSundoToken());

        List<UserBasicProfileDto> users = response.body().jsonPath().getList("users", UserBasicProfileDto.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(users)
                .extracting("id")
                .containsExactly(UserFixture.SUNDO_ID);
    }
}
