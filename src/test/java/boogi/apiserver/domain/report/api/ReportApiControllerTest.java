package boogi.apiserver.domain.report.api;

import boogi.apiserver.domain.report.application.ReportCommandService;
import boogi.apiserver.domain.report.domain.ReportReason;
import boogi.apiserver.domain.report.domain.ReportTarget;
import boogi.apiserver.domain.report.dto.request.CreateReportRequest;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.constant.SessionInfoConst;
import boogi.apiserver.utils.controller.TestControllerSetUp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = ReportApiController.class)
class ReportApiControllerTest extends TestControllerSetUp {

    @MockBean
    private ReportCommandService reportCommandService;

    @Test
    @DisplayName("신고 생성하기")
    void testCreateReport() throws Exception {
        CreateReportRequest request =
                new CreateReportRequest(1L, ReportTarget.COMMENT, ReportReason.SWEAR, "신고");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        ResultActions result = mvc.perform(
                post("/api/reports/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(request))
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH-TOKEN")
        );

        result
                .andExpect(status().isOk())
                .andDo(document("report/post",
                        requestFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("신고할 대상의 ID"),
                                fieldWithPath("target").type(JsonFieldType.STRING)
                                        .description("신고 대상으로 COMMENT(댓글), MESSAGE(쪽지), POST(게시글), COMMUNITY(커뮤니티) 중 하나 선택"),
                                fieldWithPath("reason").type(JsonFieldType.STRING)
                                        .description("신고 사유로 SEXUAL(음란물), SWEAR(욕설), DEFAMATION(명예훼손), POLITICS(정치인 비하 및 선거운동), COMMERCIAL_AD(상업적 광고), ILLEGAL_FILMING(불법 촬영물), ETC(기타)중 하나 선택"),
                                fieldWithPath("content").type(JsonFieldType.STRING)
                                        .description("신고 내용")
                        )
                ));
    }
}