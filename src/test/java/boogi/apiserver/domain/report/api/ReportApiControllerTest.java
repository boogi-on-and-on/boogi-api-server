package boogi.apiserver.domain.report.api;

import boogi.apiserver.domain.comment.exception.CommentNotFoundException;
import boogi.apiserver.domain.community.community.exception.CommunityNotFoundException;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.message.message.exception.MessageNotFoundException;
import boogi.apiserver.domain.message.message.exception.NotParticipatedUserException;
import boogi.apiserver.domain.post.post.exception.PostNotFoundException;
import boogi.apiserver.domain.report.application.ReportCommandService;
import boogi.apiserver.domain.report.domain.ReportReason;
import boogi.apiserver.domain.report.domain.ReportTarget;
import boogi.apiserver.domain.report.dto.request.CreateReportRequest;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.utils.controller.TestControllerSetUp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
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

    @Nested
    @DisplayName("신고 생성하기")
    class CreateReport {
        @Test
        @DisplayName("신고 생성에 성공한다")
        void createReportSuccess() throws Exception {
            CreateReportRequest request =
                    new CreateReportRequest(1L, ReportTarget.COMMENT, ReportReason.SWEAR, "신고");

            ResultActions result = mvc.perform(
                    post("/api/reports/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(request))
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("reports/post",
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

        @Test
        @DisplayName("커뮤니티 신고시 존재하지 않는 커뮤니티 ID로 요청할시 CommunityNotFoundException 발생")
        void notExistCommunityFail() throws Exception {
            CreateReportRequest request =
                    new CreateReportRequest(9999L, ReportTarget.COMMUNITY, ReportReason.SWEAR, "신고");

            doThrow(new CommunityNotFoundException())
                    .when(reportCommandService).createReport(any(CreateReportRequest.class), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/reports/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(request))
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("reports/post-CommunityNotFoundException"));
        }

        @Test
        @DisplayName("게시글 신고시 존재하지 않는 게시글 ID로 요청할시 PostNotFoundException 발생")
        void notExistPostFail() throws Exception {
            CreateReportRequest request =
                    new CreateReportRequest(9999L, ReportTarget.POST, ReportReason.SWEAR, "신고");

            doThrow(new PostNotFoundException())
                    .when(reportCommandService).createReport(any(CreateReportRequest.class), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/reports/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(request))
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("reports/post-PostNotFoundException"));
        }

        @Test
        @DisplayName("게시글 신고시 게시글이 작성된 커뮤니티의 멤버가 아닐시 NotJoinedMemberException 발생")
        void notMemberPostFail() throws Exception {
            CreateReportRequest request =
                    new CreateReportRequest(1L, ReportTarget.POST, ReportReason.SWEAR, "신고");

            doThrow(new NotJoinedMemberException())
                    .when(reportCommandService).createReport(any(CreateReportRequest.class), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/reports/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(request))
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("reports/post-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("댓글 신고시 존재하지 않는 댓글 ID로 요청할시 CommentNotFoundException 발생")
        void notExistCommentFail() throws Exception {
            CreateReportRequest request =
                    new CreateReportRequest(9999L, ReportTarget.COMMENT, ReportReason.SWEAR, "신고");

            doThrow(new CommentNotFoundException())
                    .when(reportCommandService).createReport(any(CreateReportRequest.class), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/reports/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(request))
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("reports/post-CommentNotFoundException"));
        }

        @Test
        @DisplayName("댓글 신고시 댓글이 작성된 커뮤니티의 멤버가 아닐시 NotJoinedMemberException 발생")
        void notMemberCommentFail() throws Exception {
            CreateReportRequest request =
                    new CreateReportRequest(1L, ReportTarget.COMMENT, ReportReason.SWEAR, "신고");

            doThrow(new NotJoinedMemberException())
                    .when(reportCommandService).createReport(any(CreateReportRequest.class), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/reports/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(request))
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("reports/post-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("쪽지 신고시 존재하지 않는 쪽지 ID로 요청할시 MessageNotFoundException 발생")
        void notExistMessageFail() throws Exception {
            CreateReportRequest request =
                    new CreateReportRequest(9999L, ReportTarget.MESSAGE, ReportReason.SWEAR, "신고");

            doThrow(new MessageNotFoundException())
                    .when(reportCommandService).createReport(any(CreateReportRequest.class), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/reports/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(request))
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("reports/post-MessageNotFoundException"));
        }

        @Test
        @DisplayName("쪽지 신고시 본인이 대화에 참여하지 않은 쪽지를 요청할시 NotParticipatedUserException 발생")
        void notParticipatedMessageFail() throws Exception {
            CreateReportRequest request =
                    new CreateReportRequest(1L, ReportTarget.MESSAGE, ReportReason.SWEAR, "신고");

            doThrow(new NotParticipatedUserException())
                    .when(reportCommandService).createReport(any(CreateReportRequest.class), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/reports/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(request))
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("reports/post-NotParticipatedUserException"));
        }
    }
}