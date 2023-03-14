package boogi.apiserver.domain.alarm.alarm.api;

import boogi.apiserver.domain.alarm.alarm.application.AlarmCommandService;
import boogi.apiserver.domain.alarm.alarm.application.AlarmQueryService;
import boogi.apiserver.domain.alarm.alarm.dto.dto.AlarmsDto;
import boogi.apiserver.domain.alarm.alarm.dto.response.AlarmsResponse;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.utils.controller.MockHttpSessionCreator;
import boogi.apiserver.utils.controller.TestControllerSetUp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = AlarmApiController.class)
class AlarmApiControllerTest extends TestControllerSetUp {

    @MockBean
    AlarmQueryService alarmQueryService;

    @MockBean
    AlarmCommandService alarmCommandService;


    @Test
    @DisplayName("알람 목록 조회")
    void getAlarms() throws Exception {

        final AlarmsDto alarmDto = new AlarmsDto(1L, "제목", "내용", LocalDateTime.now());
        final AlarmsResponse responseDto = new AlarmsResponse(List.of(alarmDto));
        given(alarmQueryService.getAlarms(any()))
                .willReturn(responseDto);

        final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                .get("/api/alarms")
                .header(HeaderConst.AUTH_TOKEN, "TOKEN")
                .session(MockHttpSessionCreator.dummySession())
        );

        response
                .andExpect(status().isOk())
                .andDo(document("alarms/get",
                        responseFields(
                                fieldWithPath("alarms")
                                        .type(JsonFieldType.ARRAY)
                                        .description("알람 목록"),

                                fieldWithPath("alarms[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("알람 ID"),

                                fieldWithPath("alarms[].head")
                                        .type(JsonFieldType.STRING)
                                        .description("알람 제목"),

                                fieldWithPath("alarms[].body")
                                        .type(JsonFieldType.STRING)
                                        .description("알람 내용"),

                                fieldWithPath("alarms[].createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("알람 생성일")
                        )
                ));
    }

    @Test
    @DisplayName("알림 삭제 테스트")
    void deleteAlarm() throws Exception {
        final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                .post("/api/alarms/{alarmId}/delete", 1L)
                .header(HeaderConst.AUTH_TOKEN, "TOKEN")
                .session(MockHttpSessionCreator.dummySession())
        );

        response
                .andExpect(status().isOk())
                .andDo(document("alarms/post-alarmId-delete",
                        pathParameters(
                                parameterWithName("alarmId").description("알람 ID")
                        )
                ));
    }
}