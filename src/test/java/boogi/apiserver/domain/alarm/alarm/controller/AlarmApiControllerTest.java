package boogi.apiserver.domain.alarm.alarm.controller;

import boogi.apiserver.domain.alarm.alarm.dto.dto.AlarmsDto;
import boogi.apiserver.domain.alarm.alarm.dto.response.AlarmsResponse;
import boogi.apiserver.domain.alarm.alarm.exception.AlarmNotFoundException;
import boogi.apiserver.domain.alarm.alarm.exception.CanNotDeleteAlarmException;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.utils.controller.ControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AlarmApiControllerTest extends ControllerTest {

    @Test
    @DisplayName("알람 목록 조회")
    void getAlarms() throws Exception {
        final AlarmsDto alarmDto = new AlarmsDto(1L, "제목", "내용", LocalDateTime.now());
        final AlarmsResponse responseDto = new AlarmsResponse(List.of(alarmDto));
        given(alarmQuery.getAlarms(any()))
                .willReturn(responseDto);

        final ResultActions response = mvc.perform(
                get("/api/alarms")
                        .header(HeaderConst.AUTH_TOKEN, TOKEN)
                        .session(dummySession)
        );

        response
                .andExpect(status().isOk())
                .andDo(document("alarms/get",
                        responseFields(
                                fieldWithPath("alarms").type(JsonFieldType.ARRAY)
                                        .description("알람 목록"),
                                fieldWithPath("alarms[].id").type(JsonFieldType.NUMBER)
                                        .description("알람 ID"),
                                fieldWithPath("alarms[].head").type(JsonFieldType.STRING)
                                        .description("알람 제목"),
                                fieldWithPath("alarms[].body").type(JsonFieldType.STRING)
                                        .description("알람 내용"),
                                fieldWithPath("alarms[].createdAt").type(JsonFieldType.STRING)
                                        .description("알람 생성일")
                        )
                ));
    }

    @Nested
    @DisplayName("알림 삭제 테스트")
    class DeleteAlarm {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            final ResultActions result = mvc.perform(
                    post("/api/alarms/{alarmId}/delete", 1L)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .session(dummySession)
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("alarms/post-alarmId-delete",
                            pathParameters(
                                    parameterWithName("alarmId").description("알람 ID")
                            )
                    ));
        }

        @Test
        @DisplayName("자신의 알람이 아닌경우 throw CanNotDeleteAlarmException")
        void notSameUser() throws Exception {
            doThrow(new CanNotDeleteAlarmException())
                    .when(alarmCommand).deleteAlarm(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/alarms/{alarmId}/delete", 1L)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .session(dummySession)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("alarms/post-alarmId-delete-CanNotDeleteAlarmException"));
        }

        @Test
        @DisplayName("없는 알람의 ID를 요청했을때 AlarmNotFoundException 발생")
        void notExistAlarm() throws Exception {
            doThrow(new AlarmNotFoundException())
                    .when(alarmCommand).deleteAlarm(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/alarms/{alarmId}/delete", 999L)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .session(dummySession)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("alarms/post-alarmId-delete-AlarmNotFoundException"));
        }
    }
}