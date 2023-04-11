package boogi.apiserver.domain.notice.controller;


import boogi.apiserver.domain.community.community.exception.CommunityNotFoundException;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.member.exception.NotOperatorException;
import boogi.apiserver.domain.notice.application.NoticeCommandService;
import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.dto.dto.NoticeDetailDto;
import boogi.apiserver.domain.notice.dto.dto.NoticeDto;
import boogi.apiserver.domain.notice.dto.request.NoticeCreateRequest;
import boogi.apiserver.domain.notice.dto.response.NoticeDetailResponse;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = NoticeApiController.class)
class NoticeApiControllerTest extends TestControllerSetUp {

    @MockBean
    NoticeQueryService noticeQueryService;

    @MockBean
    MemberQueryService memberQueryService;

    @MockBean
    NoticeCommandService noticeCommandService;

    @Test
    @DisplayName("공지 목록을 전체 조회한다.")
    void getAllNoticeSuccess() throws Exception {
        NoticeDetailDto noticeDto =
                new NoticeDetailDto(1L, "공지사항의 제목입니다.", LocalDateTime.now(), "공지사항의 내용입니다.");
        NoticeDetailResponse response = new NoticeDetailResponse(List.of(noticeDto), true);

        given(noticeQueryService.getCommunityNotice(anyLong(), anyLong()))
                .willReturn(response);

        final ResultActions result = mvc.perform(
                get("/api/notices")
                        .queryParam("communityId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN)
        );

        result
                .andExpect(status().isOk())
                .andDo(document("notices/get",
                        requestParameters(
                                parameterWithName("communityId").description("커뮤니티의 공지사항 조회인 경우").optional()
                        ),

                        responseFields(
                                fieldWithPath("manager").type(JsonFieldType.BOOLEAN)
                                        .description("true -> 커뮤니티의 공지 조회 && 조회를 요청하는 맴버가 매니저인 경우").optional(),
                                fieldWithPath("notices").type(JsonFieldType.ARRAY)
                                        .description("공지사항 목록"),
                                fieldWithPath("notices[].content").type(JsonFieldType.STRING)
                                        .description("글 내용"),
                                fieldWithPath("notices[].id").type(JsonFieldType.NUMBER)
                                        .description("공지사항 ID"),
                                fieldWithPath("notices[].title").type(JsonFieldType.STRING)
                                        .description("글 제목"),
                                fieldWithPath("notices[].createdAt").type(JsonFieldType.STRING)
                                        .description("공지사항 생성시각")
                        )
                ));
    }

    @Nested
    @DisplayName("공지사항 생성")
    class CreateNotice {
        @Test
        @DisplayName("공지사항 생성에 성공한다.")
        void createNoticeSuccess() throws Exception {
            final long NEW_NOTICE_ID = 2L;
            NoticeCreateRequest request = new NoticeCreateRequest(1L, "내용", "제목");

            given(noticeCommandService.createNotice(any(), anyLong()))
                    .willReturn(NEW_NOTICE_ID);

            final ResultActions result = mvc.perform(
                    post("/api/notices")
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("notices/post",
                            requestFields(
                                    fieldWithPath("communityId").type(JsonFieldType.NUMBER)
                                            .description("커뮤니티 ID"),
                                    fieldWithPath("title").type(JsonFieldType.STRING)
                                            .description("공지사항 제목")
                                            .attributes(key("constraint").value("5 ~ 30 길이의 문자열")),
                                    fieldWithPath("content").type(JsonFieldType.STRING)
                                            .description("공지사항 내용")
                                            .attributes(key("constraint").value("10 ~ 1000 길이의 문자열"))
                            ),
                            responseFields(
                                    fieldWithPath("id").type(JsonFieldType.NUMBER)
                                            .description("생성된 공지사항의 ID")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 커뮤니티 ID로 요청한 경우 CommunityNotFoundException 발생")
        void notExistCommunityFail() throws Exception {
            NoticeCreateRequest request = new NoticeCreateRequest(9999L, "내용", "제목");

            doThrow(new CommunityNotFoundException())
                    .when(noticeCommandService).createNotice(any(NoticeCreateRequest.class), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/notices")
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("notices/post-CommunityNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티에 가입되지 않는 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            NoticeCreateRequest request = new NoticeCreateRequest(1L, "내용", "제목");

            doThrow(new NotJoinedMemberException())
                    .when(noticeCommandService).createNotice(any(NoticeCreateRequest.class), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/notices")
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("notices/post-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("해당 커뮤니티의 관리자가 아닌 경우 NotOperatorException 발생")
        void notOperatorFail() throws Exception {
            NoticeCreateRequest request = new NoticeCreateRequest(1L, "내용", "제목");

            doThrow(new NotOperatorException())
                    .when(noticeCommandService).createNotice(any(NoticeCreateRequest.class), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/notices")
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("notices/post-NotOperatorException"));
        }
    }

    @DisplayName("최근 앱 공지사항 목록 조회")
    @Test
    void latestAppNotice() throws Exception {
        final NoticeDto noticeDto = new NoticeDto(1L, "제목", LocalDateTime.now());

        given(noticeQueryService.getAppLatestNotice())
                .willReturn(List.of(noticeDto));

        final ResultActions result = mvc.perform(
                get("/api/notices/recent")
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN)
        );

        result
                .andExpect(status().isOk())
                .andDo(document("notices/get-recent",
                        responseFields(
                                fieldWithPath("notices").type(JsonFieldType.ARRAY)
                                        .description("최근 앱 공지사항 목록"),
                                fieldWithPath("notices[].id").type(JsonFieldType.NUMBER)
                                        .description("공지사항 ID"),
                                fieldWithPath("notices[].title").type(JsonFieldType.STRING)
                                        .description("공지사항 제목"),
                                fieldWithPath("notices[].createdAt").type(JsonFieldType.STRING)
                                        .description("공지사항 생성 시각")
                        )
                ));
    }
}