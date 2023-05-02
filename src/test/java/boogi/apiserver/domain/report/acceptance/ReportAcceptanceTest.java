package boogi.apiserver.domain.report.acceptance;

import boogi.apiserver.domain.message.message.dto.request.SendMessageRequest;
import boogi.apiserver.domain.report.domain.ReportReason;
import boogi.apiserver.domain.report.domain.ReportTarget;
import boogi.apiserver.domain.report.dto.request.CreateReportRequest;
import boogi.apiserver.utils.AcceptanceTest;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static boogi.apiserver.utils.fixture.CommentFixture.createNewComment;
import static boogi.apiserver.utils.fixture.CommunityFixture.BASEBALL_ID;
import static boogi.apiserver.utils.fixture.HttpMethodFixture.httpPost;
import static boogi.apiserver.utils.fixture.PostFixture.createNewPost;
import static boogi.apiserver.utils.fixture.TokenFixture.getSundoToken;
import static boogi.apiserver.utils.fixture.UserFixture.YONGJIN_ID;
import static org.assertj.core.api.Assertions.assertThat;

public class ReportAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("커뮤니티를 신고한다.")
    void createCommunityReport() {
        CreateReportRequest request =
                new CreateReportRequest(BASEBALL_ID, ReportTarget.COMMUNITY, ReportReason.ETC, "커뮤니티를 신고합니다.");

        ExtractableResponse<Response> response = httpPost(request, "/reports/", getSundoToken());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("게시글을 신고한다.")
    void createPostReport() {
        long newPostId = createNewPost();

        CreateReportRequest request =
                new CreateReportRequest(newPostId, ReportTarget.POST, ReportReason.ETC, "게시글을 신고합니다.");

        ExtractableResponse<Response> response = httpPost(request, "/reports/", getSundoToken());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("댓글을 신고한다.")
    void createCommentReport() {
        long newCommentId = createNewComment();

        CreateReportRequest request =
                new CreateReportRequest(newCommentId, ReportTarget.COMMENT, ReportReason.ETC, "댓글을 신고합니다.");

        ExtractableResponse<Response> response = httpPost(request, "/reports/", getSundoToken());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("쪽지를 신고한다.")
    void createMessageReport() {
        SendMessageRequest sendRequest = new SendMessageRequest(YONGJIN_ID, "안녕 나는 선도야");
        Long newMessageId = httpPost(sendRequest, "/messages/", getSundoToken()).body().jsonPath().getLong("id");

        CreateReportRequest request =
                new CreateReportRequest(newMessageId, ReportTarget.MESSAGE, ReportReason.ETC, "쪽지를 신고합니다.");

        ExtractableResponse<Response> response = httpPost(request, "/reports/", getSundoToken());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }
}
