package boogi.apiserver.domain.notice.acceptance;

import boogi.apiserver.domain.notice.dto.dto.CommunityNoticeDetailDto;
import boogi.apiserver.domain.notice.dto.request.NoticeCreateRequest;
import boogi.apiserver.utils.AcceptanceTest;
import boogi.apiserver.utils.fixture.CommunityFixture;
import boogi.apiserver.utils.fixture.HttpMethodFixture;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static boogi.apiserver.utils.fixture.TokenFixture.getSundoToken;
import static org.assertj.core.api.Assertions.assertThat;

public class NoticeAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("커뮤니티 공지사항 추가 시, 커뮤니티 공지사항 목록에 조회된다.")
    void createCommunityNotice() {
        NoticeCreateRequest noticeCreateRequest = new NoticeCreateRequest(CommunityFixture.POCS_ID, "이번주 모임 공지", "이번 주 모임은 공학관에서 진행합니다. 많은 참여 부탁드립니다.");
        long newNoticeId = HttpMethodFixture.httpPost(noticeCreateRequest, "/notices", getSundoToken())
                .body().jsonPath().getLong("id");

        ExtractableResponse<Response> response =
                HttpMethodFixture.httpGet("/notices?communityId=" + CommunityFixture.POCS_ID, getSundoToken());

        List<CommunityNoticeDetailDto> notices = response.body().jsonPath().getList("notices", CommunityNoticeDetailDto.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(notices)
                .extracting("id")
                .contains(newNoticeId);
    }
}
